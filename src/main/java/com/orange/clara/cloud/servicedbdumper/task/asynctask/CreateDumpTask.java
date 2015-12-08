package com.orange.clara.cloud.servicedbdumper.task.asynctask;

import com.orange.clara.cloud.servicedbdumper.dbdumper.running.Dumper;
import com.orange.clara.cloud.servicedbdumper.exception.DumpException;
import com.orange.clara.cloud.servicedbdumper.model.Job;
import com.orange.clara.cloud.servicedbdumper.model.JobEvent;
import com.orange.clara.cloud.servicedbdumper.repo.JobRepo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
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
public class CreateDumpTask {
    private Logger logger = LoggerFactory.getLogger(CreateDumpTask.class);
    @Autowired
    @Qualifier("dumper")
    private Dumper dumper;

    @Autowired
    private JobRepo jobRepo;

    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Future<Boolean> runCreateDump(Integer jobId) {
        Job job = this.jobRepo.findOne(jobId);
        try {
            this.dumper.dump(job.getDatabaseRefSrc());
        } catch (DumpException e) {
            logger.error(String.format("Cannot create dump for '%s': %s", job.getDatabaseRefSrc().getName(), e.getMessage()));
            job.setJobEvent(JobEvent.ERRORED);
            jobRepo.save(job);
            return new AsyncResult<Boolean>(false);
        }
        job.setJobEvent(JobEvent.FINISHED);
        jobRepo.save(job);
        return new AsyncResult<Boolean>(true);
    }
}