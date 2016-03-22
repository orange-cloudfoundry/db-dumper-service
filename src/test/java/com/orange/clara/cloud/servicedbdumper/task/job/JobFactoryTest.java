package com.orange.clara.cloud.servicedbdumper.task.job;

import com.google.common.collect.Lists;
import com.orange.clara.cloud.servicedbdumper.model.*;
import com.orange.clara.cloud.servicedbdumper.repo.JobRepo;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Date;

import static org.fest.assertions.Assertions.assertThat;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.eq;
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
public class JobFactoryTest {
    private final static int jobErroredDeleteExpirationDays = 1;
    private final static int jobFinishedDeleteExpirationMinutes = 1;
    private static Job job;
    @InjectMocks
    JobFactory jobFactory;
    @Mock
    JobRepo jobRepo;

    @Before
    public void init() {
        initMocks(this);
        jobFactory.jobErroredDeleteExpirationDays = jobErroredDeleteExpirationDays;
        jobFactory.jobFinishedDeleteExpirationMinutes = jobFinishedDeleteExpirationMinutes;
        when(jobRepo.save((Job) notNull())).thenAnswer(invocation -> {
            job = (Job) invocation.getArguments()[0];
            return job;
        });
    }

    @Test
    public void when_create_job_with_only_database_ref_source_and_no_jobs_with_same_shape_exist_it_should_create_a_started_job() {
        DatabaseRef databaseRef = new DatabaseRef();
        DbDumperServiceInstance dbDumperServiceInstance = new DbDumperServiceInstance();
        jobFactory.createJobWithDatabaseRefSrc(JobType.CREATE_DUMP, databaseRef, dbDumperServiceInstance);
        assertThat(job).isNotNull();
        assertThat(job.getJobType()).isEqualTo(JobType.CREATE_DUMP);
        assertThat(job.getJobEvent()).isEqualTo(JobEvent.START);
        assertThat(job.getDatabaseRefSrc()).isEqualTo(databaseRef);
        assertThat(job.getDbDumperServiceInstance()).isEqualTo(dbDumperServiceInstance);

    }

    @Test
    public void when_create_job_with_database_ref_source_and_target_and_no_jobs_with_same_shape_exist_it_should_create_a_started_job() {
        DatabaseRef databaseRef = new DatabaseRef();
        Date date = new Date();
        DbDumperServiceInstance dbDumperServiceInstance = new DbDumperServiceInstance();
        jobFactory.createJob(JobType.CREATE_DUMP, databaseRef, databaseRef, date, dbDumperServiceInstance);
        assertThat(job).isNotNull();
        assertThat(job.getJobType()).isEqualTo(JobType.CREATE_DUMP);
        assertThat(job.getJobEvent()).isEqualTo(JobEvent.START);
        assertThat(job.getDumpDate()).isEqualTo(date);
        assertThat(job.getDatabaseRefSrc()).isEqualTo(databaseRef);
        assertThat(job.getDatabaseRefTarget()).isEqualTo(databaseRef);
        assertThat(job.getDbDumperServiceInstance()).isEqualTo(dbDumperServiceInstance);

    }

    @Test
    public void when_create_job_with_only_database_ref_source_and_job_with_same_shape_already_exists_it_should_create_a_started_job() {
        DatabaseRef databaseRef = new DatabaseRef();
        DbDumperServiceInstance dbDumperServiceInstance = new DbDumperServiceInstance();

        when(jobRepo.findByJobTypeAndJobEventAndDatabaseRefSrc(anyObject(), eq(JobEvent.START), anyObject())).thenReturn(Arrays.asList(new Job()));
        jobFactory.createJobWithDatabaseRefSrc(JobType.CREATE_DUMP, databaseRef, dbDumperServiceInstance);
        assertThat(job).isNotNull();
        assertThat(job.getJobType()).isEqualTo(JobType.CREATE_DUMP);
        assertThat(job.getJobEvent()).isEqualTo(JobEvent.SCHEDULED);
        assertThat(job.getDatabaseRefSrc()).isEqualTo(databaseRef);
        assertThat(job.getDbDumperServiceInstance()).isEqualTo(dbDumperServiceInstance);

        when(jobRepo.findByJobTypeAndJobEventAndDatabaseRefSrc(anyObject(), eq(JobEvent.START), anyObject())).thenReturn(Lists.newArrayList());
        when(jobRepo.findByJobTypeAndJobEventAndDatabaseRefSrc(anyObject(), eq(JobEvent.RUNNING), anyObject())).thenReturn(Arrays.asList(job));
        jobFactory.createJobWithDatabaseRefSrc(JobType.CREATE_DUMP, databaseRef, dbDumperServiceInstance);
        assertThat(job).isNotNull();
        assertThat(job.getJobType()).isEqualTo(JobType.CREATE_DUMP);
        assertThat(job.getJobEvent()).isEqualTo(JobEvent.SCHEDULED);
        assertThat(job.getDatabaseRefSrc()).isEqualTo(databaseRef);
        assertThat(job.getDbDumperServiceInstance()).isEqualTo(dbDumperServiceInstance);
    }

    @Test
    public void when_create_job_with_database_ref_source_and_target_and_job_with_same_shape_already_exists_it_should_create_a_started_job() {
        DatabaseRef databaseRef = new DatabaseRef();
        DbDumperServiceInstance dbDumperServiceInstance = new DbDumperServiceInstance();
        Date date = new Date();
        when(jobRepo.findByJobTypeAndJobEventAndDatabaseRefSrcAndDatabaseRefTarget(anyObject(), eq(JobEvent.START), anyObject(), anyObject())).thenReturn(Arrays.asList(new Job()));
        jobFactory.createJob(JobType.CREATE_DUMP, databaseRef, databaseRef, date, dbDumperServiceInstance);
        assertThat(job).isNotNull();
        assertThat(job.getJobType()).isEqualTo(JobType.CREATE_DUMP);
        assertThat(job.getJobEvent()).isEqualTo(JobEvent.SCHEDULED);
        assertThat(job.getDumpDate()).isEqualTo(date);
        assertThat(job.getDatabaseRefSrc()).isEqualTo(databaseRef);
        assertThat(job.getDatabaseRefTarget()).isEqualTo(databaseRef);
        assertThat(job.getDbDumperServiceInstance()).isEqualTo(dbDumperServiceInstance);

        when(jobRepo.findByJobTypeAndJobEventAndDatabaseRefSrcAndDatabaseRefTarget(anyObject(), eq(JobEvent.START), anyObject(), anyObject())).thenReturn(Lists.newArrayList());
        when(jobRepo.findByJobTypeAndJobEventAndDatabaseRefSrcAndDatabaseRefTarget(anyObject(), eq(JobEvent.RUNNING), anyObject(), anyObject())).thenReturn(Arrays.asList(job));
        jobFactory.createJob(JobType.CREATE_DUMP, databaseRef, databaseRef, date, dbDumperServiceInstance);
        assertThat(job).isNotNull();
        assertThat(job.getJobType()).isEqualTo(JobType.CREATE_DUMP);
        assertThat(job.getJobEvent()).isEqualTo(JobEvent.SCHEDULED);
        assertThat(job.getDumpDate()).isEqualTo(date);
        assertThat(job.getDatabaseRefSrc()).isEqualTo(databaseRef);
        assertThat(job.getDatabaseRefTarget()).isEqualTo(databaseRef);
        assertThat(job.getDbDumperServiceInstance()).isEqualTo(dbDumperServiceInstance);
    }

    @Test
    public void when_purge_errored_jobs_and_no_job_in_error_exist_it_should_not_delete_job() {
        when(jobRepo.findByJobEventOrderByUpdatedAtDesc(anyObject())).thenReturn(Lists.newArrayList());
        jobFactory.purgeErroredJobs();
        verify(jobRepo, times(0)).delete((Job) notNull());
    }

    @Test
    public void when_purge_errored_jobs_and_jobs_in_error_exist_it_should_delete_job_which_pass_expiration() {
        Job jobNotExpired = new Job();
        jobNotExpired.setUpdatedAt(new Date());

        Date date = new Date();
        LocalDateTime localDateTime = LocalDateTime.from(date.toInstant().atZone(ZoneId.systemDefault())).minusDays(jobErroredDeleteExpirationDays + 1);
        Instant instant = localDateTime.atZone(ZoneId.systemDefault()).toInstant();
        Job jobExpired = new Job();
        jobExpired.setUpdatedAt(Date.from(instant));

        when(jobRepo.findByJobEventOrderByUpdatedAtDesc(anyObject())).thenReturn(Arrays.asList(jobNotExpired, jobExpired));
        jobFactory.purgeErroredJobs();
        verify(jobRepo, times(1)).delete((Job) notNull());
    }

    @Test
    public void when_purge_finished_jobs_and_no_job_in_error_exist_it_should_not_delete_job() {
        when(jobRepo.findByJobEventOrderByUpdatedAtDesc(anyObject())).thenReturn(Lists.newArrayList());
        jobFactory.purgeFinishedJob();
        verify(jobRepo, times(0)).delete((Job) notNull());
    }

    @Test
    public void when_purge_finished_jobs_and_jobs_in_error_exist_it_should_delete_job_which_pass_expiration_and_have_null_database_target_and_source_and_service_instance() {
        Job jobNotExpired = new Job();
        jobNotExpired.setUpdatedAt(new Date());
        jobNotExpired.setDatabaseRefSrc(new DatabaseRef());

        Date date = new Date();
        LocalDateTime localDateTime = LocalDateTime.from(date.toInstant().atZone(ZoneId.systemDefault())).minusMinutes(jobFinishedDeleteExpirationMinutes + 1);
        Instant instant = localDateTime.atZone(ZoneId.systemDefault()).toInstant();
        Job jobExpired = new Job();
        jobExpired.setUpdatedAt(Date.from(instant));

        when(jobRepo.findByJobEventOrderByUpdatedAtDesc(anyObject())).thenReturn(Arrays.asList(jobNotExpired, jobExpired));
        jobFactory.purgeFinishedJob();
        verify(jobRepo, times(1)).delete((Job) notNull());
    }

    @Test
    public void ensure_it_has_transactional_annotations() throws NoSuchMethodException {
        JobFactory.class.getMethod("createJob", JobType.class, DatabaseRef.class, DatabaseRef.class, Date.class, DbDumperServiceInstance.class).isAnnotationPresent(Transactional.class);
        JobFactory.class.getMethod("createJobWithDatabaseRefSrc", JobType.class, DatabaseRef.class, DbDumperServiceInstance.class).isAnnotationPresent(Transactional.class);
    }
}