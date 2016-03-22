package com.orange.clara.cloud.servicedbdumper.task;

import com.google.common.collect.Lists;
import com.orange.clara.cloud.servicedbdumper.dbdumper.Deleter;
import com.orange.clara.cloud.servicedbdumper.model.DatabaseDumpFile;
import com.orange.clara.cloud.servicedbdumper.model.Job;
import com.orange.clara.cloud.servicedbdumper.model.JobEvent;
import com.orange.clara.cloud.servicedbdumper.repo.DatabaseDumpFileRepo;
import com.orange.clara.cloud.servicedbdumper.repo.JobRepo;
import com.orange.clara.cloud.servicedbdumper.task.job.JobFactory;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static org.fest.assertions.Assertions.assertThat;
import static org.mockito.Matchers.anyObject;
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
 * Date: 22/03/2016
 */
public class ScheduledManagingJobTaskTest extends AbstractScheduledTaskTest {
    private final static int dumpDeleteExpirationDays = 1;
    @InjectMocks
    ScheduledManagingJobTask scheduledTask;
    @Mock
    JobRepo jobRepo;
    @Mock
    DatabaseDumpFileRepo databaseDumpFileRepo;
    @Mock
    Deleter deleter;
    @Mock
    JobFactory jobFactory;

    @Before
    public void init() {
        initMocks(this);
        scheduledTask.dumpDeleteExpirationDays = dumpDeleteExpirationDays;
    }

    @Test
    public void when_cleaning_finished_job_it_should_call_purge_from_job_factory() {
        scheduledTask.cleaningFinishedJobs();
        verify(jobFactory, times(1)).purgeJob();
    }

    @Test
    public void when_run_scheduled_job_and_job_with_same_shape_already_running_it_should_stay_as_a_scheduled_job() {
        Job job = new Job();
        job.setJobEvent(JobEvent.SCHEDULED);
        when(jobRepo.findByJobEventOrderByUpdatedAtDesc((JobEvent) notNull())).thenReturn(Arrays.asList(job));
        when(jobRepo.findByJobTypeAndJobEventAndDatabaseRefSrcAndDatabaseRefTarget(anyObject(), eq(JobEvent.START), anyObject(), anyObject())).thenReturn(Arrays.asList(job));
        scheduledTask.startScheduledJobs();
        assertThat(job.getJobEvent()).isEqualTo(JobEvent.SCHEDULED);

        when(jobRepo.findByJobTypeAndJobEventAndDatabaseRefSrcAndDatabaseRefTarget(anyObject(), eq(JobEvent.START), anyObject(), anyObject())).thenReturn(Lists.newArrayList());
        when(jobRepo.findByJobTypeAndJobEventAndDatabaseRefSrcAndDatabaseRefTarget(anyObject(), eq(JobEvent.RUNNING), anyObject(), anyObject())).thenReturn(Arrays.asList(job));
        scheduledTask.startScheduledJobs();
        assertThat(job.getJobEvent()).isEqualTo(JobEvent.SCHEDULED);
    }

    @Test
    public void when_run_scheduled_job_it_should_put_job_as_started() {
        Job job = new Job();
        job.setJobEvent(JobEvent.SCHEDULED);
        when(jobRepo.findByJobEventOrderByUpdatedAtDesc((JobEvent) notNull())).thenReturn(Arrays.asList(job));
        when(jobRepo.findByJobTypeAndJobEventAndDatabaseRefSrcAndDatabaseRefTarget(anyObject(), anyObject(), anyObject(), anyObject())).thenReturn(Lists.newArrayList());
        scheduledTask.startScheduledJobs();
        assertThat(job.getJobEvent()).isEqualTo(JobEvent.START);

    }

    @Test
    public void when_there_is_no_dump_file_to_delete_it_should_not_call_deleter() {
        when(databaseDumpFileRepo.findByDeletedTrueOrderByDeletedAtAsc()).thenReturn(Lists.newArrayList());
        scheduledTask.cleaningDeletedDumpFile();
        verify(deleter, times(0)).delete(anyObject());
    }

    @Test
    public void when_there_is_dump_files_to_delete_it_should_only_delete_dump_file_wich_pass_expiration_days() {
        Date date = new Date();
        LocalDateTime localDateTime = LocalDateTime.from(date.toInstant().atZone(ZoneId.systemDefault())).minusDays(dumpDeleteExpirationDays + 1);
        Instant instant = localDateTime.atZone(ZoneId.systemDefault()).toInstant();

        DatabaseDumpFile databaseDumpFileNotExpired = new DatabaseDumpFile();
        databaseDumpFileNotExpired.setDeletedAt(new Date());

        DatabaseDumpFile databaseDumpFileExpired = new DatabaseDumpFile();
        databaseDumpFileExpired.setDeletedAt(Date.from(instant));

        List<DatabaseDumpFile> databaseDumpFiles = Lists.newArrayList();
        databaseDumpFiles.add(databaseDumpFileExpired);
        databaseDumpFiles.add(databaseDumpFileNotExpired);

        when(databaseDumpFileRepo.findByDeletedTrueOrderByDeletedAtAsc()).thenReturn(databaseDumpFiles);
        scheduledTask.cleaningDeletedDumpFile();
        verify(deleter, times(1)).delete(anyObject());
    }

    @Test
    public void ensure_it_has_required_annotations() throws NoSuchMethodException {
        this.assertMethodsHaveScheduledAnnotations(ScheduledManagingJobTask.class, "cleaningFinishedJobs", "cleaningDeletedDumpFile", "alerting", "startScheduledJobs");
        this.assertMethodsHaveTransactionalAnnotations(ScheduledManagingJobTask.class, "cleaningFinishedJobs", "cleaningDeletedDumpFile");
    }
}