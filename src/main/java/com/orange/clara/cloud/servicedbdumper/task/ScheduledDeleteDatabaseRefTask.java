package com.orange.clara.cloud.servicedbdumper.task;

import com.orange.clara.cloud.servicedbdumper.exception.JobCreationException;
import com.orange.clara.cloud.servicedbdumper.model.DatabaseRef;
import com.orange.clara.cloud.servicedbdumper.model.Job;
import com.orange.clara.cloud.servicedbdumper.model.JobEvent;
import com.orange.clara.cloud.servicedbdumper.model.JobType;
import com.orange.clara.cloud.servicedbdumper.repo.DatabaseRefRepo;
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

import java.util.List;

/**
 * Copyright (C) 2015 Orange
 * <p>
 * This software is distributed under the terms and conditions of the 'Apache-2.0'
 * license which can be found in the file 'LICENSE' in this package distribution
 * or at 'https://opensource.org/licenses/Apache-2.0'.
 * <p>
 * Author: Arthur Halet
 * Date: 25/11/2015
 */
@Component
public class ScheduledDeleteDatabaseRefTask {

    private Logger logger = LoggerFactory.getLogger(ScheduledDeleteDatabaseRefTask.class);

    @Autowired
    private JobRepo jobRepo;

    @Autowired
    private DatabaseRefRepo databaseRefRepo;

    @Autowired
    @Qualifier("jobFactory")
    private JobFactory jobFactory;

    @Scheduled(fixedDelay = 5000)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void deleteDatabaseRef() throws JobCreationException {
        List<Job> jobs = jobRepo.findByJobTypeAndJobEvent(JobType.DELETE_DATABASE_REF, JobEvent.START);

        logger.debug("Running: delete database reference scheduled task ...");

        for (Job job : jobs) {
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
                job.setErrorMessage(e.getMessage());
                jobRepo.save(job);
                continue;
            }

            job.setJobEvent(JobEvent.FINISHED);
            jobRepo.save(job);
        }
        logger.debug("Finished: delete database reference scheduled task ...");

    }
}
