package com.orange.clara.cloud.servicedbdumper.task;

import com.orange.clara.cloud.servicedbdumper.dbdumper.running.Dumper;
import com.orange.clara.cloud.servicedbdumper.exception.DumpException;
import com.orange.clara.cloud.servicedbdumper.model.Job;
import com.orange.clara.cloud.servicedbdumper.model.JobState;
import com.orange.clara.cloud.servicedbdumper.repo.JobRepo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

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
public class ScheduledCreateDumpTask {
    private Logger logger = LoggerFactory.getLogger(ScheduledCreateDumpTask.class);
    @Autowired
    @Qualifier("dumper")
    private Dumper dumper;

    @Autowired
    private JobRepo jobRepo;

    @Scheduled(fixedRate = 5000)
    public void createDump() {
        logger.info("Running create dump scheduled task ...");
        for (Job job : jobRepo.findByJobState(JobState.CREATE_DUMP)) {
            job.setJobState(JobState.RUNNING);
            jobRepo.save(job);
            try {
                this.dumper.dump(job.getDatabaseRef());
            } catch (DumpException e) {
                logger.error(String.format("Cannot create dump for '%s': %s", job.getDatabaseRef().getName(), e.getMessage()));
                job.setJobState(JobState.ERRORED);
                jobRepo.save(job);
                continue;
            }
            jobRepo.delete(job);
        }
    }
}