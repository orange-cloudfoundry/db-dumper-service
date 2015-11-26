package com.orange.clara.cloud.servicedbdumper.task;

import com.orange.clara.cloud.servicedbdumper.dbdumper.running.Deleter;
import com.orange.clara.cloud.servicedbdumper.model.DatabaseRef;
import com.orange.clara.cloud.servicedbdumper.model.Job;
import com.orange.clara.cloud.servicedbdumper.model.JobState;
import com.orange.clara.cloud.servicedbdumper.repo.DatabaseRefRepo;
import com.orange.clara.cloud.servicedbdumper.repo.JobRepo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Calendar;

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
public class ScheduledDeleteDumpTask {
    private Logger logger = LoggerFactory.getLogger(ScheduledDeleteDumpTask.class);
    @Value("${dump.delete.expiration.days:5}")
    private Integer dumpDeleteExpirationDays;
    @Autowired
    private JobRepo jobRepo;

    @Autowired
    @Qualifier("deleter")
    private Deleter deleter;

    @Autowired
    private DatabaseRefRepo databaseRefRepo;

    @Scheduled(fixedRate = 5000)
    public void deleteDump() {
        logger.info("Running delete all dump scheduled task ...");
        LocalDateTime localDateTime;
        for (Job job : jobRepo.findByJobState(JobState.DELETE_DUMPS)) {
            localDateTime = LocalDateTime.from(job.getUpdatedAt().toInstant()).plusDays(this.dumpDeleteExpirationDays);
            if (LocalDateTime.from(Calendar.getInstance().toInstant()).isBefore(localDateTime)) {
                continue;
            }
            job.setJobState(JobState.RUNNING);
            jobRepo.save(job);
            DatabaseRef databaseRef = job.getDatabaseRef();
            this.deleter.deleteAll(databaseRef);
            if (databaseRef.isDeleted()) {
                this.databaseRefRepo.delete(databaseRef);
            }
            jobRepo.save(new Job(JobState.DELETE_DATABASE_REF, job.getDatabaseRef()));
            jobRepo.delete(job);

        }
    }
}