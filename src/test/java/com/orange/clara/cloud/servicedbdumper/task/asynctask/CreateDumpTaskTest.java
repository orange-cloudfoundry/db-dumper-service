package com.orange.clara.cloud.servicedbdumper.task.asynctask;

import com.orange.clara.cloud.servicedbdumper.dbdumper.DatabaseRefManager;
import com.orange.clara.cloud.servicedbdumper.dbdumper.Dumper;
import com.orange.clara.cloud.servicedbdumper.exception.AsyncTaskException;
import com.orange.clara.cloud.servicedbdumper.exception.DatabaseExtractionException;
import com.orange.clara.cloud.servicedbdumper.exception.DumpException;
import com.orange.clara.cloud.servicedbdumper.model.DatabaseDumpFile;
import com.orange.clara.cloud.servicedbdumper.model.DbDumperServiceInstance;
import com.orange.clara.cloud.servicedbdumper.model.JobEvent;
import com.orange.clara.cloud.servicedbdumper.model.Metadata;
import com.orange.clara.cloud.servicedbdumper.repo.DatabaseDumpFileRepo;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import static org.fest.assertions.Assertions.assertThat;
import static org.mockito.Matchers.notNull;
import static org.mockito.Mockito.*;

/**
 * Copyright (C) 2016 Arthur Halet
 * <p>
 * This software is distributed under the terms and conditions of the 'MIT'
 * license which can be found in the file 'LICENSE' in this package distribution
 * or at 'http://opensource.org/licenses/MIT'.
 * <p>
 * Author: Arthur Halet
 * Date: 21/03/2016
 */
public class CreateDumpTaskTest extends AbstractTaskTest {
    @InjectMocks
    CreateDumpTask createDumpTask;
    @Mock
    Dumper dumper;
    @Mock
    DatabaseRefManager databaseRefManager;

    @Mock
    DatabaseDumpFileRepo databaseDumpFileRepo;
    private DatabaseDumpFile databaseDumpFile;

    public CreateDumpTaskTest() {
        super(CreateDumpTask.class);
    }

    @Override
    @Before
    public void init() throws DatabaseExtractionException {
        super.init();
        databaseDumpFile = new DatabaseDumpFile();
        try {
            when(dumper.dump((DbDumperServiceInstance) notNull())).thenAnswer(invocation -> databaseDumpFile);
        } catch (DumpException e) {
            throw new DatabaseExtractionException(e.getMessage(), e);
        }
    }

    @Test
    public void when_dump_creation_failed_it_should_update_the_job_to_be_failed() throws DumpException, ExecutionException, InterruptedException, AsyncTaskException {
        this.assertJobStatusBefore();
        doThrow(new DumpException("error")).when(dumper).dump((DbDumperServiceInstance) notNull());
        Future<Boolean> result = this.createDumpTask.runTask(1);
        assertThat(job.getJobEvent()).isEqualTo(JobEvent.ERRORED);
        assertThat(job.getErrorMessage()).isNotEmpty();
        assertThat(result.get()).isFalse();
    }

    @Test
    public void when_dump_creation_it_should_update_the_job_to_be_finished() throws DumpException, ExecutionException, InterruptedException, AsyncTaskException {
        this.assertJobStatusBefore();
        Future<Boolean> result = this.createDumpTask.runTask(1);
        assertThat(job.getJobEvent()).isEqualTo(JobEvent.FINISHED);
        assertThat(result.get()).isTrue();
    }

    @Test
    public void when_dump_creation_have_metadata_it_should_pass_it_to_dump_and_update_the_job_to_be_finished() throws DumpException, ExecutionException, InterruptedException, AsyncTaskException {
        this.assertJobStatusBefore();
        assertThat(databaseDumpFile.getMetadata()).isNull();
        Metadata metadata = new Metadata();
        job.setMetadata(metadata);
        Future<Boolean> result = this.createDumpTask.runTask(1);
        assertThat(job.getJobEvent()).isEqualTo(JobEvent.FINISHED);
        assertThat(result.get()).isTrue();
        assertThat(databaseDumpFile.getMetadata()).isNotNull();
        verify(databaseDumpFileRepo, times(1)).save((DatabaseDumpFile) notNull());
    }
}