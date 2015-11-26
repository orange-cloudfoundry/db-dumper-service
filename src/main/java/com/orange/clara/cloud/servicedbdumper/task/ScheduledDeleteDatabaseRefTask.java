package com.orange.clara.cloud.servicedbdumper.task;

import com.orange.clara.cloud.servicedbdumper.model.DatabaseRef;
import com.orange.clara.cloud.servicedbdumper.model.Job;
import com.orange.clara.cloud.servicedbdumper.model.JobState;
import com.orange.clara.cloud.servicedbdumper.repo.DatabaseRefRepo;
import com.orange.clara.cloud.servicedbdumper.repo.DbDumperServiceInstanceBindingRepo;
import com.orange.clara.cloud.servicedbdumper.repo.DbDumperServiceInstanceRepo;
import com.orange.clara.cloud.servicedbdumper.repo.JobRepo;
import org.springframework.beans.factory.annotation.Autowired;
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
public class ScheduledDeleteDatabaseRefTask {

    @Autowired
    private JobRepo jobRepo;

    @Autowired
    private DbDumperServiceInstanceBindingRepo serviceInstanceBindingRepository;

    @Autowired
    private DbDumperServiceInstanceRepo serviceInstanceRepo;

    @Autowired
    private DatabaseRefRepo databaseRefRepo;

    @Scheduled(fixedRate = 5000)
    public void deleteInstance() {
        for (Job job : jobRepo.findByJobState(JobState.DELETE_DATABASE_REF)) {
            job.setJobState(JobState.RUNNING);
            jobRepo.save(job);
            DatabaseRef databaseRef = job.getDatabaseRef();
            if (!databaseRef.isDeleted()) {
                continue;
            }
            if (databaseRef.getDatabaseDumpFiles().size() > 0) {
                jobRepo.save(new Job(JobState.DELETE_DUMPS, databaseRef));
                jobRepo.delete(job);
                continue;
            }
            databaseRefRepo.delete(databaseRef);
            jobRepo.delete(job);
        }
    }
}
