package com.orange.clara.cloud.servicedbdumper.task;

import com.google.common.collect.Lists;
import com.orange.clara.cloud.servicedbdumper.exception.AsyncTaskException;
import com.orange.clara.cloud.servicedbdumper.exception.JobCreationException;
import com.orange.clara.cloud.servicedbdumper.model.*;
import com.orange.clara.cloud.servicedbdumper.repo.DatabaseRefRepo;
import com.orange.clara.cloud.servicedbdumper.repo.JobRepo;
import com.orange.clara.cloud.servicedbdumper.task.job.JobFactory;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.util.Arrays;

import static org.fest.assertions.Assertions.assertThat;
import static org.mockito.Matchers.notNull;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;

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
public class ScheduledDeleteDatabaseRefTaskTest extends AbstractScheduledTaskTest {

    @InjectMocks
    ScheduledDeleteDatabaseRefTask scheduledTask;

    @Mock
    JobRepo jobRepo;

    @Mock
    DatabaseRefRepo databaseRefRepo;

    @Mock
    JobFactory jobFactory;

    private Job job;
    private DatabaseRef databaseRef;

    @Before
    public void init() {
        initMocks(this);
        job = new Job();
        databaseRef = new DatabaseRef();
        job.setDatabaseRefSrc(databaseRef);
        when(jobRepo.findByJobTypeAndJobEvent((JobType) notNull(), (JobEvent) notNull())).thenReturn(Lists.newArrayList());
    }

    @Test
    public void when_there_is_no_delete_database_ref_job_it_should_not_run_dump_deletion_and_not_delete_database_ref() throws AsyncTaskException, JobCreationException {
        scheduledTask.deleteDatabaseRef();
        verify(databaseRefRepo, times(0)).delete((DatabaseRef) notNull());
    }

    @Test
    public void when_job_have_database_which_is_not_deleted_it_should_not_run_dump_deletion_and_not_delete_database_ref() throws AsyncTaskException, JobCreationException {
        when(jobRepo.findByJobTypeAndJobEvent((JobType) notNull(), (JobEvent) notNull())).thenReturn(Arrays.asList(job));
        scheduledTask.deleteDatabaseRef();
        verify(databaseRefRepo, times(0)).delete((DatabaseRef) notNull());
    }

    @Test
    public void when_job_have_database_which_still_have_dump_file_it_should_run_dump_deletion_but_not_delete_database_ref() throws AsyncTaskException, JobCreationException {
        when(jobRepo.findByJobTypeAndJobEvent((JobType) notNull(), (JobEvent) notNull())).thenReturn(Arrays.asList(job));
        databaseRef.setDeleted(true);
        databaseRef.addDatabaseDumpFile(new DatabaseDumpFile());
        scheduledTask.deleteDatabaseRef();
        verify(databaseRefRepo, times(0)).delete((DatabaseRef) notNull());
        verify(jobFactory, times(1)).createJobDeleteDumps((DatabaseRef) notNull(), anyObject());
        assertThat(job.getJobEvent()).isEqualTo(JobEvent.FINISHED);
    }

    @Test
    public void when_job_have_database_which_dont_have_dump_file_it_should_not_run_dump_deletion_and_delete_database_ref() throws AsyncTaskException, JobCreationException {
        when(jobRepo.findByJobTypeAndJobEvent((JobType) notNull(), (JobEvent) notNull())).thenReturn(Arrays.asList(job));
        databaseRef.setDeleted(true);
        scheduledTask.deleteDatabaseRef();
        verify(databaseRefRepo, times(1)).delete(databaseRef);
        verify(jobFactory, times(0)).createJobDeleteDumps((DatabaseRef) notNull(), anyObject());
        assertThat(job.getJobEvent()).isEqualTo(JobEvent.FINISHED);
    }

    @Test
    public void when_job_have_database_which_dont_have_dump_file_but_deletion_database_failed_it_should_put_the_job_in_error() throws AsyncTaskException, JobCreationException {
        when(jobRepo.findByJobTypeAndJobEvent((JobType) notNull(), (JobEvent) notNull())).thenReturn(Arrays.asList(job));
        databaseRef.setDeleted(true);
        doThrow(new RuntimeException("error")).when(databaseRefRepo).delete((DatabaseRef) notNull());
        scheduledTask.deleteDatabaseRef();
        verify(databaseRefRepo, times(1)).delete(databaseRef);
        verify(jobFactory, times(0)).createJobDeleteDumps((DatabaseRef) notNull(), anyObject());
        assertThat(job.getJobEvent()).isEqualTo(JobEvent.ERRORED);
        assertThat(job.getErrorMessage()).isNotEmpty();
    }

    @Test
    public void ensure_it_has_required_annotations() throws NoSuchMethodException {
        this.assertMethodsHaveScheduledAnnotations(ScheduledDeleteDatabaseRefTask.class, "deleteDatabaseRef");
        this.assertMethodsHaveTransactionalAnnotations(ScheduledDeleteDatabaseRefTask.class, "deleteDatabaseRef");
    }
}