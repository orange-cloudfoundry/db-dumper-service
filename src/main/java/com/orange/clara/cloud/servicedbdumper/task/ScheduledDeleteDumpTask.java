package com.orange.clara.cloud.servicedbdumper.task;

import com.orange.clara.cloud.servicedbdumper.model.Job;
import com.orange.clara.cloud.servicedbdumper.model.JobEvent;
import com.orange.clara.cloud.servicedbdumper.model.JobType;
import com.orange.clara.cloud.servicedbdumper.repo.DatabaseRefRepo;
import com.orange.clara.cloud.servicedbdumper.repo.JobRepo;
import com.orange.clara.cloud.servicedbdumper.task.asynctask.DeleteDumpTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneId;
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
    private DatabaseRefRepo databaseRefRepo;

    @Autowired
    @Qualifier(value = "deleteDumpTask")
    private DeleteDumpTask deleteDumpTask;

    @Scheduled(fixedDelay = 5000)
    public void deleteDump() {
        logger.info("Running delete all dump scheduled task ...");
        LocalDateTime whenRemoveDateTime;
        for (Job job : jobRepo.findByJobTypeAndJobEvent(JobType.DELETE_DUMPS, JobEvent.START)) {
            whenRemoveDateTime = LocalDateTime.from(job.getUpdatedAt().toInstant().atZone(ZoneId.of("UTC"))).plusDays(this.dumpDeleteExpirationDays);
            if (LocalDateTime.from(Calendar.getInstance().toInstant().atZone(ZoneId.of("UTC"))).isBefore(whenRemoveDateTime)) {
                continue;
            }
            job.setJobEvent(JobEvent.RUNNING);
            jobRepo.save(job);
            this.deleteDumpTask.runDeleteDump(job.getId());
        }
    }
}