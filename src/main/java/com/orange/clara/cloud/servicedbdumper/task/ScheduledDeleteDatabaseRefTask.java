package com.orange.clara.cloud.servicedbdumper.task;

import com.orange.clara.cloud.servicedbdumper.model.DatabaseRef;
import com.orange.clara.cloud.servicedbdumper.model.Job;
import com.orange.clara.cloud.servicedbdumper.model.JobEvent;
import com.orange.clara.cloud.servicedbdumper.model.JobType;
import com.orange.clara.cloud.servicedbdumper.repo.DatabaseRefRepo;
import com.orange.clara.cloud.servicedbdumper.repo.DbDumperServiceInstanceBindingRepo;
import com.orange.clara.cloud.servicedbdumper.repo.DbDumperServiceInstanceRepo;
import com.orange.clara.cloud.servicedbdumper.repo.JobRepo;
import com.orange.clara.cloud.servicedbdumper.task.job.JobFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Copyright (C) 2015 Orange
 * <p/>
 * This software is distributed under the terms and conditions of the 'MIT'
 * license which can be found in the file 'LICENSE' in this package distribution
 * or at 'http://opensource.org/licenses/MIT'.
 * <p/>
 * Author: Arthur Halet
 * Date: 25/11/2015
 */
@Component
public class ScheduledDeleteDatabaseRefTask {

    private Logger logger = LoggerFactory.getLogger(ScheduledDeleteDatabaseRefTask.class);

    @Autowired
    private JobRepo jobRepo;

    @Autowired
    private DbDumperServiceInstanceBindingRepo serviceInstanceBindingRepository;

    @Autowired
    private DbDumperServiceInstanceRepo serviceInstanceRepo;

    @Autowired
    private DatabaseRefRepo databaseRefRepo;

    @Autowired
    @Qualifier("jobFactory")
    private JobFactory jobFactory;

    @Scheduled(fixedDelay = 5000)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void deleteDatabaseRef() {
        logger.info("Running: delete database reference scheduled task ...");
        for (Job job : jobRepo.findByJobTypeAndJobEvent(JobType.DELETE_DATABASE_REF, JobEvent.START)) {
            job.setJobEvent(JobEvent.RUNNING);
            jobRepo.save(job);
            DatabaseRef databaseRef = job.getDatabaseRefSrc();
            if (!databaseRef.isDeleted()) {
                continue;
            }
            if (databaseRef.getDatabaseDumpFiles().size() > 0) {
                this.jobFactory.createJobDeleteDumps(databaseRef, null);

                job.setJobEvent(JobEvent.FINISHED);
                jobRepo.save(job);
                continue;
            }
            job.setDatabaseRefSrc(null);
            jobRepo.save(job);
            try {
                databaseRefRepo.delete(databaseRef);
            } catch (Exception e) {
                job.setJobEvent(JobEvent.ERRORED);
                jobRepo.save(job);
                continue;
            }

            job.setJobEvent(JobEvent.FINISHED);
            jobRepo.save(job);
        }
        logger.info("Finished: delete database reference scheduled task ...");
    }
}
