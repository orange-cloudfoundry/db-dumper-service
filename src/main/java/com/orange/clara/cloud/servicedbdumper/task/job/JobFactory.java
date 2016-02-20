package com.orange.clara.cloud.servicedbdumper.task.job;

import com.orange.clara.cloud.servicedbdumper.dbdumper.DatabaseRefManager;
import com.orange.clara.cloud.servicedbdumper.model.*;
import com.orange.clara.cloud.servicedbdumper.repo.JobRepo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Copyright (C) 2015 Orange
 * <p>
 * This software is distributed under the terms and conditions of the 'Apache-2.0'
 * license which can be found in the file 'LICENSE' in this package distribution
 * or at 'https://opensource.org/licenses/Apache-2.0'.
 * <p>
 * Author: Arthur Halet
 * Date: 27/11/2015
 */
public class JobFactory {

    @Value("${job.errored.delete.expiration.days:2}")
    private Integer jobErroredDeleteExpirationDays;
    @Value("${job.finished.delete.expiration.minutes:8}")
    private Integer jobFinishedDeleteExpirationMinutes;

    private Logger logger = LoggerFactory.getLogger(JobFactory.class);
    @Autowired
    private JobRepo jobRepo;

    @Autowired
    private DatabaseRefManager databaseRefManager;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void createJob(JobType jobType, DatabaseRef databaseRefSrc, DatabaseRef databaseRefTarget, Date dumpDate, DbDumperServiceInstance dbDumperServiceInstance) {
        Job job = new Job(jobType, databaseRefSrc, databaseRefTarget, dumpDate, dbDumperServiceInstance);
        if (this.jobRepo.findByJobTypeAndJobEventAndDatabaseRefSrcAndDatabaseRefTarget(jobType, JobEvent.START, databaseRefSrc, databaseRefTarget).size() > 0
                || this.jobRepo.findByJobTypeAndJobEventAndDatabaseRefSrcAndDatabaseRefTarget(jobType, JobEvent.RUNNING, databaseRefSrc, databaseRefTarget).size() > 0) {
            job.setJobEvent(JobEvent.SCHEDULED);
            this.logger.info(
                    String.format("Job type: %s for database source '%s' and database target '%s' has been scheduled.",
                            jobType.toString(),
                            databaseRefSrc.getDatabaseName(),
                            databaseRefTarget.getDatabaseName()
                    )
            );
        }
        this.jobRepo.save(job);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void createJobWithDatabaseRefSrc(JobType jobType, DatabaseRef databaseRefSrc, DbDumperServiceInstance dbDumperServiceInstance) {
        Job job = new Job(jobType, databaseRefSrc, dbDumperServiceInstance);
        if (this.jobRepo.findByJobTypeAndJobEventAndDatabaseRefSrc(jobType, JobEvent.START, databaseRefSrc).size() > 0
                || this.jobRepo.findByJobTypeAndJobEventAndDatabaseRefSrc(jobType, JobEvent.RUNNING, databaseRefSrc).size() > 0) {
            job.setJobEvent(JobEvent.SCHEDULED);
            this.logger.info(
                    String.format("Job type: %s for database source '%s' has been scheduled.",
                            jobType.toString(),
                            databaseRefSrc.getDatabaseName()
                    )
            );
        }
        this.jobRepo.save(job);
    }

    public void createJobCreateDump(DbDumperServiceInstance dbDumperServiceInstance) {
        this.createJobWithDatabaseRefSrc(JobType.CREATE_DUMP, dbDumperServiceInstance.getDatabaseRef(), dbDumperServiceInstance);
    }

    public void createJobDeleteDumps(DbDumperServiceInstance dbDumperServiceInstance) {
        this.createJobWithDatabaseRefSrc(JobType.DELETE_DUMPS, dbDumperServiceInstance.getDatabaseRef(), dbDumperServiceInstance);
    }

    public void createJobRestoreDump(DatabaseRef databaseRefTarget, Date createdAt, DbDumperServiceInstance dbDumperServiceInstance) {
        this.createJob(JobType.RESTORE_DUMP, dbDumperServiceInstance.getDatabaseRef(), databaseRefTarget, createdAt, dbDumperServiceInstance);
    }

    public void createJobCreateDump(DatabaseRef databaseRefSrc, DbDumperServiceInstance dbDumperServiceInstance) {
        this.createJobWithDatabaseRefSrc(JobType.CREATE_DUMP, databaseRefSrc, dbDumperServiceInstance);
    }

    public void createJobDeleteDumps(DatabaseRef databaseRefSrc, DbDumperServiceInstance dbDumperServiceInstance) {
        this.createJobWithDatabaseRefSrc(JobType.DELETE_DUMPS, databaseRefSrc, dbDumperServiceInstance);
    }

    public void createJobDeleteDatabaseRef(DatabaseRef databaseRefSrc) {
        this.createJobWithDatabaseRefSrc(JobType.DELETE_DATABASE_REF, databaseRefSrc, null);
    }

    public void createJobRestoreDump(DatabaseRef databaseRefSrc, DatabaseRef databaseRefTarget, Date createdAt, DbDumperServiceInstance dbDumperServiceInstance) {
        this.createJob(JobType.RESTORE_DUMP, databaseRefSrc, databaseRefTarget, createdAt, dbDumperServiceInstance);
    }

    @Transactional
    public void purgeJob() {
        this.purgeErroredJobs();
        this.purgeFinishedJob();
    }

    @Transactional
    public void purgeErroredJobs() {
        LocalDateTime whenRemoveDateTime;
        List<Job> jobs = jobRepo.findByJobEventOrderByUpdatedAtDesc(JobEvent.ERRORED);
        for (Job job : jobs) {
            whenRemoveDateTime = LocalDateTime.from(job.getUpdatedAt().toInstant().atZone(ZoneId.of("UTC"))).plusDays(this.jobErroredDeleteExpirationDays);
            if (LocalDateTime.from(Calendar.getInstance().toInstant().atZone(ZoneId.of("UTC"))).isBefore(whenRemoveDateTime)) {
                continue;
            }
            this.jobRepo.delete(job);
        }
    }

    @Transactional
    public void purgeFinishedJob() {
        LocalDateTime whenRemoveDateTime;
        List<Job> jobs = jobRepo.findByJobEventOrderByUpdatedAtDesc(JobEvent.FINISHED);
        for (Job job : jobs) {
            whenRemoveDateTime = LocalDateTime.from(job.getUpdatedAt().toInstant().atZone(ZoneId.of("UTC"))).plusMinutes(this.jobFinishedDeleteExpirationMinutes);
            if (LocalDateTime.from(Calendar.getInstance().toInstant().atZone(ZoneId.of("UTC"))).isBefore(whenRemoveDateTime)
                    && (job.getDatabaseRefSrc() != null || job.getDbDumperServiceInstance() != null || job.getDatabaseRefTarget() != null)) {
                continue;
            }
            this.jobRepo.delete(job);
        }
    }
}
