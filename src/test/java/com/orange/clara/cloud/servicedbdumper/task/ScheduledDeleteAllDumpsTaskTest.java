package com.orange.clara.cloud.servicedbdumper.task;

import com.google.common.collect.Lists;
import com.orange.clara.cloud.servicedbdumper.exception.AsyncTaskException;
import com.orange.clara.cloud.servicedbdumper.exception.JobCreationException;
import com.orange.clara.cloud.servicedbdumper.model.Job;
import com.orange.clara.cloud.servicedbdumper.model.JobEvent;
import com.orange.clara.cloud.servicedbdumper.model.JobType;
import com.orange.clara.cloud.servicedbdumper.repo.JobRepo;
import com.orange.clara.cloud.servicedbdumper.task.asynctask.DeleteDumpTask;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Date;

import static org.mockito.Matchers.anyInt;
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
public class ScheduledDeleteAllDumpsTaskTest extends AbstractScheduledTaskTest {
    private final static Integer dumpDeleteExpirationDays = 1;
    @InjectMocks
    ScheduledDeleteAllDumpsTask scheduledTask;

    @Mock
    JobRepo jobRepo;

    @Mock
    DeleteDumpTask deleteDumpTask;

    @Before
    public void init() {
        initMocks(this);
        scheduledTask.dumpDeleteExpirationDays = dumpDeleteExpirationDays;
        when(jobRepo.findByJobTypeAndJobEvent((JobType) notNull(), (JobEvent) notNull())).thenReturn(Lists.newArrayList());
    }

    @Test
    public void when_there_is_no_delete_dump_job_it_should_not_run_dump_deletion() throws AsyncTaskException, JobCreationException {
        scheduledTask.deleteAllDumps();
        verify(deleteDumpTask, times(0)).runTask(anyInt());
    }

    @Test
    public void when_there_is_delete_dump_jobs_but_expiration_days_not_passed_it_should_not_run_dump_deletion() throws AsyncTaskException, JobCreationException {
        Job job = new Job();
        job.setDumpDate(new Date());
        when(jobRepo.findByJobTypeAndJobEvent((JobType) notNull(), (JobEvent) notNull())).thenReturn(Arrays.asList(job));
        scheduledTask.deleteAllDumps();
        verify(deleteDumpTask, times(0)).runTask(anyInt());
    }

    @Test
    public void when_there_is_delete_dump_jobs_and_expiration_days_passed_it_should_run_dump_deletion() throws AsyncTaskException, JobCreationException {
        Job job1 = new Job();
        job1.setUpdatedAt(new Date());
        Date date = new Date();
        LocalDateTime localDateTime = LocalDateTime.from(date.toInstant().atZone(ZoneId.systemDefault())).minusDays(dumpDeleteExpirationDays + 1);
        Instant instant = localDateTime.atZone(ZoneId.systemDefault()).toInstant();
        Job job2 = new Job();
        job2.setUpdatedAt(Date.from(instant));
        when(jobRepo.findByJobTypeAndJobEvent((JobType) notNull(), (JobEvent) notNull())).thenReturn(Arrays.asList(job1, job2));
        scheduledTask.deleteAllDumps();
        verify(deleteDumpTask, times(1)).runTask(anyInt());
    }

    @Test
    public void ensure_it_has_scheduled_annotation() throws NoSuchMethodException {
        this.assertMethodsHaveScheduledAnnotations(ScheduledDeleteAllDumpsTask.class, "deleteAllDumps");
    }
}