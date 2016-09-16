package com.orange.clara.cloud.servicedbdumper.task;

import com.google.common.collect.Lists;
import com.orange.clara.cloud.servicedbdumper.exception.AsyncTaskException;
import com.orange.clara.cloud.servicedbdumper.exception.JobCreationException;
import com.orange.clara.cloud.servicedbdumper.model.*;
import com.orange.clara.cloud.servicedbdumper.repo.DatabaseRefRepo;
import com.orange.clara.cloud.servicedbdumper.repo.DbDumperServiceInstanceRepo;
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
public class ScheduledDeleteDbDumperServiceInstanceTaskTest extends AbstractScheduledTaskTest {

    @InjectMocks
    ScheduledDeleteDbDumperServiceInstanceTask scheduledTask;

    @Mock
    JobRepo jobRepo;

    @Mock
    DatabaseRefRepo databaseRefRepo;

    @Mock
    DbDumperServiceInstanceRepo serviceInstanceRepo;

    @Mock
    JobFactory jobFactory;

    private Job job;
    private DatabaseRef databaseRef;
    private DbDumperServiceInstance dbDumperServiceInstance;

    @Before
    public void init() {
        initMocks(this);
        job = new Job();
        databaseRef = new DatabaseRef();
        dbDumperServiceInstance = new DbDumperServiceInstance();
        dbDumperServiceInstance.setDatabaseRef(databaseRef);
        job.setDbDumperServiceInstance(dbDumperServiceInstance);
        when(jobRepo.findByJobTypeAndJobEvent((JobType) notNull(), (JobEvent) notNull())).thenReturn(Lists.newArrayList());
    }

    @Test
    public void when_there_is_no_delete_service_instance_job_it_should_not_run_dump_deletion_and_not_delete_service_instance() throws AsyncTaskException, JobCreationException {
        scheduledTask.deleteDbDumperServiceInstance();
        verify(serviceInstanceRepo, times(0)).delete((DbDumperServiceInstance) notNull());
    }

    @Test
    public void when_job_have_service_instance_which_is_not_deleted_it_should_not_run_dump_deletion_and_not_delete_service_instance() throws AsyncTaskException, JobCreationException {
        when(jobRepo.findByJobTypeAndJobEvent((JobType) notNull(), (JobEvent) notNull())).thenReturn(Arrays.asList(job));
        scheduledTask.deleteDbDumperServiceInstance();
        verify(serviceInstanceRepo, times(0)).delete((DbDumperServiceInstance) notNull());
    }

    @Test
    public void when_job_have_service_instance_which_still_have_dump_file_it_should_run_dump_deletion_but_not_delete_service_instance() throws AsyncTaskException, JobCreationException {
        when(jobRepo.findByJobTypeAndJobEvent((JobType) notNull(), (JobEvent) notNull())).thenReturn(Arrays.asList(job));
        dbDumperServiceInstance.setDeleted(true);
        dbDumperServiceInstance.addDatabaseDumpFile(new DatabaseDumpFile());
        scheduledTask.deleteDbDumperServiceInstance();
        verify(serviceInstanceRepo, times(0)).delete((DbDumperServiceInstance) notNull());
        verify(jobFactory, times(1)).createJobDeleteDumps((DatabaseRef) notNull(), (DbDumperServiceInstance) notNull());
        assertThat(job.getJobEvent()).isEqualTo(JobEvent.FINISHED);
    }

    @Test
    public void when_job_have_service_instance_which_dont_have_dump_file_it_should_not_run_dump_deletion_and_delete_service_instance() throws AsyncTaskException, JobCreationException {
        when(jobRepo.findByJobTypeAndJobEvent((JobType) notNull(), (JobEvent) notNull())).thenReturn(Arrays.asList(job));
        dbDumperServiceInstance.setDeleted(true);
        scheduledTask.deleteDbDumperServiceInstance();
        verify(serviceInstanceRepo, times(1)).delete(dbDumperServiceInstance);
        verify(jobFactory, times(0)).createJobDeleteDumps((DatabaseRef) notNull(), (DbDumperServiceInstance) notNull());
        assertThat(job.getJobEvent()).isEqualTo(JobEvent.FINISHED);
    }

    @Test
    public void when_job_have_service_instance_which_dont_have_dump_file_but_deletion_service_instance_failed_it_should_put_the_job_in_error() throws AsyncTaskException, JobCreationException {
        when(jobRepo.findByJobTypeAndJobEvent((JobType) notNull(), (JobEvent) notNull())).thenReturn(Arrays.asList(job));
        dbDumperServiceInstance.setDeleted(true);
        doThrow(new RuntimeException("error")).when(serviceInstanceRepo).delete((DbDumperServiceInstance) notNull());
        scheduledTask.deleteDbDumperServiceInstance();
        verify(serviceInstanceRepo, times(1)).delete(dbDumperServiceInstance);
        verify(jobFactory, times(0)).createJobDeleteDumps((DatabaseRef) notNull(), (DbDumperServiceInstance) notNull());
        assertThat(job.getJobEvent()).isEqualTo(JobEvent.ERRORED);
        assertThat(job.getErrorMessage()).isNotEmpty();
    }

    @Test
    public void ensure_it_has_required_annotations() throws NoSuchMethodException {
        this.assertMethodsHaveScheduledAnnotations(ScheduledDeleteDbDumperServiceInstanceTask.class, "deleteDbDumperServiceInstance");
        this.assertMethodsHaveTransactionalAnnotations(ScheduledDeleteDbDumperServiceInstanceTask.class, "deleteDbDumperServiceInstance");
    }
}