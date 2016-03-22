package com.orange.clara.cloud.servicedbdumper.task.asynctask;

import com.orange.clara.cloud.servicedbdumper.dbdumper.Deleter;
import com.orange.clara.cloud.servicedbdumper.exception.AsyncTaskException;
import com.orange.clara.cloud.servicedbdumper.model.JobEvent;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import static org.fest.assertions.Assertions.assertThat;

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
public class DeleteDumpTaskTest extends AbstractTaskTest {
    @InjectMocks
    DeleteDumpTask deleteDumpTask;

    @Mock
    Deleter deleter;

    public DeleteDumpTaskTest() {
        super(DeleteDumpTask.class);
    }

    @Test
    public void when_delete_dump_it_should_update_the_job_to_be_finished() throws ExecutionException, InterruptedException, AsyncTaskException {
        this.assertJobStatusBefore();
        Future<Boolean> result = this.deleteDumpTask.runTask(1);
        assertThat(job.getJobEvent()).isEqualTo(JobEvent.FINISHED);
        assertThat(result.get()).isTrue();
    }
}