package com.orange.clara.cloud.servicedbdumper.task;

import com.orange.clara.cloud.servicedbdumper.model.Job;
import com.orange.clara.cloud.servicedbdumper.model.JobEvent;
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
public class ScheduledManagingJobTask {
    private Logger logger = LoggerFactory.getLogger(ScheduledManagingJobTask.class);

    @Autowired
    private JobRepo jobRepo;

    @Autowired
    @Qualifier("jobFactory")
    private JobFactory jobFactory;

    @Scheduled(fixedDelay = 300000)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void cleaningFinishedJobs() {
        logger.info("Running: cleaning job scheduled task ...");
        jobFactory.purgeJob();
        logger.info("Finished: cleaning job scheduled task.");
    }

    @Scheduled(fixedDelay = 1200000)
    public void alerting() {
        Integer numberJobsErrored = this.jobRepo.findByJobEventOrderByUpdatedAtDesc(JobEvent.ERRORED).size();
        if (numberJobsErrored > 0) {
            logger.info("There is '" + numberJobsErrored + "' jobs in error.");
        }
    }

    @Scheduled(fixedDelay = 3000)
    public void startScheduledJobs() {
        List<Job> jobs = this.jobRepo.findByJobEventOrderByUpdatedAtDesc(JobEvent.SCHEDULED);
        if (!jobs.isEmpty()) {
            logger.info("Running: starting scheduled jobs ...");
        }
        for (Job job : jobs) {
            if (this.jobRepo.findByJobTypeAndJobEventAndDatabaseRefSrcAndDatabaseRefTarget(
                    job.getJobType(),
                    JobEvent.START,
                    job.getDatabaseRefSrc(),
                    job.getDatabaseRefTarget()).size() > 0 ||
                    this.jobRepo.findByJobTypeAndJobEventAndDatabaseRefSrcAndDatabaseRefTarget(
                            job.getJobType(),
                            JobEvent.RUNNING,
                            job.getDatabaseRefSrc(),
                            job.getDatabaseRefTarget()).size() > 0) {
                continue;
            }
            logger.info("starting scheduled job " + job.getId());
            job.setJobEvent(JobEvent.START);
            this.jobRepo.save(job);
        }
        if (!jobs.isEmpty()) {
            logger.info("Finished: starting scheduled jobs ...");
        }
    }
}