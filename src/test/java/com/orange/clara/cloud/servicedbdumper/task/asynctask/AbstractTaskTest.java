package com.orange.clara.cloud.servicedbdumper.task.asynctask;

import com.orange.clara.cloud.servicedbdumper.exception.DatabaseExtractionException;
import com.orange.clara.cloud.servicedbdumper.model.DatabaseRef;
import com.orange.clara.cloud.servicedbdumper.model.DbDumperServiceInstance;
import com.orange.clara.cloud.servicedbdumper.model.Job;
import com.orange.clara.cloud.servicedbdumper.model.JobEvent;
import com.orange.clara.cloud.servicedbdumper.repo.JobRepo;
import com.orange.clara.cloud.servicedbdumper.task.job.JobFactory;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.springframework.scheduling.annotation.Async;
import org.springframework.transaction.annotation.Transactional;

import java.net.URI;

import static org.fest.assertions.Assertions.assertThat;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

/**
 * Copyright (C) 2016 Orange
 * <p>
 * This software is distributed under the terms and conditions of the 'Apache-2.0'
 * license which can be found in the file 'LICENSE' in this package distribution
 * or at 'https://opensource.org/licenses/Apache-2.0'.
 * <p>
 * Author: Arthur Halet
 * Date: 21/03/2016
 */
abstract public class AbstractTaskTest {

    protected Job job;
    @Mock
    JobRepo jobRepo;
    @Mock
    JobFactory jobFactory;

    private Class asyncTaskClass;

    public AbstractTaskTest(Class asyncTaskClass) {
        this.asyncTaskClass = asyncTaskClass;
    }

    @Before
    public void init() throws DatabaseExtractionException {
        initMocks(this);
        job = new Job();
        DatabaseRef databaseRef = new DatabaseRef("service-1", URI.create("mysql://user:pass@mysql.db:3306/mydb"));
        DbDumperServiceInstance dbDumperServiceInstance = new DbDumperServiceInstance();
        dbDumperServiceInstance.setDatabaseRef(databaseRef);
        job.setDatabaseRefSrc(databaseRef);
        job.setDatabaseRefTarget(databaseRef);
        job.setDbDumperServiceInstance(dbDumperServiceInstance);
        when(jobRepo.findOne(anyInt())).thenReturn(job);
    }

    @Test
    public void ensure_annotations_required_are_set() throws NoSuchMethodException {
        assertThat(asyncTaskClass.getMethod("runTask", Integer.class).isAnnotationPresent(Async.class)).isTrue();
        assertThat(asyncTaskClass.getMethod("runTask", Integer.class).isAnnotationPresent(Transactional.class)).isTrue();
    }

    protected void assertJobStatusBefore() {
        assertThat(job.getJobEvent()).isEqualTo(JobEvent.START);
        assertThat(job.getErrorMessage()).isNull();
    }
}
