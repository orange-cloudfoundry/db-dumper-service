package com.orange.clara.cloud.servicedbdumper.task.boot.sequences;

import com.orange.clara.cloud.servicedbdumper.model.Job;
import com.orange.clara.cloud.servicedbdumper.model.JobEvent;
import com.orange.clara.cloud.servicedbdumper.repo.JobRepo;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.util.Set;

import static org.fest.assertions.Assertions.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.notNull;
import static org.mockito.Mockito.when;
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
public class BootSequenceJobTest {
    public final static Job job = new Job();
    @InjectMocks
    BootSequenceJob bootSequence;
    @Mock
    JobRepo jobRepo;

    @Before
    public void init() {
        initMocks(this);
        when(jobRepo.updateJobFromJobEventSetToJobEventWithErrorMessage((JobEvent) notNull(), (Set<JobEvent>) notNull(), anyString())).then(invocation -> {
            job.setJobEvent(JobEvent.ERRORED);
            job.setErrorMessage(invocation.getArguments()[2].toString());
            return 1;
        });
    }

    @Test
    public void when_running_sequence_it_should_put_job_in_error() {
        assertThat(job.getErrorMessage()).isNull();
        assertThat(job.getJobEvent()).isEqualTo(JobEvent.START);
        this.bootSequence.runSequence();
        assertThat(job.getErrorMessage()).isNotEmpty();
        assertThat(job.getJobEvent()).isEqualTo(JobEvent.ERRORED);
    }
}