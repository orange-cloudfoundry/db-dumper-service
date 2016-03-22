package com.orange.clara.cloud.servicedbdumper.task;

import com.google.common.collect.Lists;
import com.orange.clara.cloud.servicedbdumper.exception.AsyncTaskException;
import com.orange.clara.cloud.servicedbdumper.model.Job;
import com.orange.clara.cloud.servicedbdumper.model.JobEvent;
import com.orange.clara.cloud.servicedbdumper.model.JobType;
import com.orange.clara.cloud.servicedbdumper.repo.JobRepo;
import com.orange.clara.cloud.servicedbdumper.task.asynctask.CreateDumpTask;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.util.Arrays;

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
public class ScheduledCreateDumpTaskTest extends AbstractScheduledTaskTest {
    @InjectMocks
    ScheduledCreateDumpTask scheduledTask = new ScheduledCreateDumpTask();

    @Mock
    CreateDumpTask createDumpTask;

    @Mock
    JobRepo jobRepo;

    @Before
    public void init() {
        initMocks(this);
        when(jobRepo.findByJobTypeAndJobEvent((JobType) notNull(), (JobEvent) notNull())).thenReturn(Lists.newArrayList());
    }

    @Test
    public void when_there_is_no_create_dump_job_it_should_not_run_dump_creation() throws AsyncTaskException {
        scheduledTask.createDump();
        verify(createDumpTask, times(0)).runTask(anyInt());
    }

    @Test
    public void when_there_is_create__dump_job_it_should_run_dump_creation() throws AsyncTaskException {
        when(jobRepo.findByJobTypeAndJobEvent((JobType) notNull(), (JobEvent) notNull())).thenReturn(Arrays.asList(new Job(), new Job()));
        scheduledTask.createDump();
        verify(createDumpTask, times(2)).runTask(anyInt());
    }

    @Test
    public void ensure_it_has_scheduled_annotation() throws NoSuchMethodException {
        this.assertMethodsHaveScheduledAnnotations(ScheduledCreateDumpTask.class, "createDump");
    }

}