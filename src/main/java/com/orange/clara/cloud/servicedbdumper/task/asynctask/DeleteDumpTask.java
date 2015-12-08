package com.orange.clara.cloud.servicedbdumper.task.asynctask;

import com.orange.clara.cloud.servicedbdumper.dbdumper.running.Deleter;
import com.orange.clara.cloud.servicedbdumper.model.DatabaseRef;
import com.orange.clara.cloud.servicedbdumper.model.Job;
import com.orange.clara.cloud.servicedbdumper.model.JobEvent;
import com.orange.clara.cloud.servicedbdumper.repo.JobRepo;
import com.orange.clara.cloud.servicedbdumper.task.job.JobFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.Future;

/**
 * Copyright (C) 2015 Orange
 * <p/>
 * This software is distributed under the terms and conditions of the 'MIT'
 * license which can be found in the file 'LICENSE' in this package distribution
 * or at 'http://opensource.org/licenses/MIT'.
 * <p/>
 * Author: Arthur Halet
 * Date: 26/11/2015
 */
public class DeleteDumpTask {

    private Logger logger = LoggerFactory.getLogger(DeleteDumpTask.class);

    @Value("${dump.delete.expiration.days:5}")
    private Integer dumpDeleteExpirationDays;
    @Autowired
    private JobRepo jobRepo;

    @Autowired
    @Qualifier("deleter")
    private Deleter deleter;

    @Autowired
    @Qualifier("jobFactory")
    private JobFactory jobFactory;

    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Future<Boolean> runDeleteDump(Integer jobId) {
        Job job = this.jobRepo.findOne(jobId);
        DatabaseRef databaseRef = job.getDatabaseRefSrc();
        this.deleter.deleteAll(databaseRef);
        this.jobFactory.createJobDeleteDatabaseRef(job.getDatabaseRefSrc(), null);

        job.setJobEvent(JobEvent.FINISHED);
        jobRepo.save(job);
        return new AsyncResult<Boolean>(true);
    }
}