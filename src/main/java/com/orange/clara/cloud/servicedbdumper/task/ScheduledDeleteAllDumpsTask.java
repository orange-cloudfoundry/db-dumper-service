package com.orange.clara.cloud.servicedbdumper.task;

import com.orange.clara.cloud.servicedbdumper.exception.JobCreationException;
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
public class ScheduledDeleteAllDumpsTask {
    private Logger logger = LoggerFactory.getLogger(ScheduledDeleteAllDumpsTask.class);
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
    public void deleteAllDumps() throws JobCreationException {
        List<Job> jobs = jobRepo.findByJobTypeAndJobEvent(JobType.DELETE_DUMPS, JobEvent.START);
        logger.debug("Running: delete all dump scheduled task ...");

        LocalDateTime whenRemoveDateTime;
        for (Job job : jobs) {
            whenRemoveDateTime = LocalDateTime.from(job.getUpdatedAt().toInstant().atZone(ZoneId.of("UTC"))).plusDays(this.dumpDeleteExpirationDays);
            if (LocalDateTime.from(Calendar.getInstance().toInstant().atZone(ZoneId.of("UTC"))).isBefore(whenRemoveDateTime)) {
                continue;
            }
            job.setJobEvent(JobEvent.RUNNING);
            jobRepo.save(job);
            this.deleteDumpTask.runDeleteDump(job.getId());
        }

        logger.debug("Finished: delete all dump scheduled task.");
    }
}