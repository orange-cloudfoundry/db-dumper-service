package com.orange.clara.cloud.servicedbdumper.task.asynctask;

import com.orange.clara.cloud.servicedbdumper.dbdumper.DatabaseRefManager;
import com.orange.clara.cloud.servicedbdumper.dbdumper.Dumper;
import com.orange.clara.cloud.servicedbdumper.exception.AsyncTaskException;
import com.orange.clara.cloud.servicedbdumper.exception.DumpException;
import com.orange.clara.cloud.servicedbdumper.model.DatabaseDumpFile;
import com.orange.clara.cloud.servicedbdumper.model.Job;
import com.orange.clara.cloud.servicedbdumper.model.JobEvent;
import com.orange.clara.cloud.servicedbdumper.repo.DatabaseDumpFileRepo;
import com.orange.clara.cloud.servicedbdumper.repo.JobRepo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.Future;

/**
 * Copyright (C) 2015 Orange
 * <p>
 * This software is distributed under the terms and conditions of the 'Apache-2.0'
 * license which can be found in the file 'LICENSE' in this package distribution
 * or at 'https://opensource.org/licenses/Apache-2.0'.
 * <p>
 * Author: Arthur Halet
 * Date: 26/11/2015
 */
public class CreateDumpTask {
    private Logger logger = LoggerFactory.getLogger(CreateDumpTask.class);
    @Autowired
    private Dumper dumper;

    @Autowired
    private JobRepo jobRepo;

    @Autowired
    private DatabaseRefManager databaseRefManager;

    @Autowired
    private DatabaseDumpFileRepo databaseDumpFileRepo;

    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Future<Boolean> runTask(Integer jobId) throws AsyncTaskException {
        Job job = this.jobRepo.findOne(jobId);
        DatabaseDumpFile databaseDumpFile = null;
        try {
            databaseDumpFile = this.dumper.dump(job.getDbDumperServiceInstance());
            if (job.getMetadata() != null) {
                logger.debug("Adding metadata for dump {}.", databaseDumpFile.getId());
                databaseDumpFile.setMetadata(job.getMetadata());
                this.databaseDumpFileRepo.save(databaseDumpFile);
                logger.debug("Finished adding metadata.");
            }
        } catch (DumpException e) {
            logger.error("Cannot create dump for '{}': {}", job.getDatabaseRefSrc().getName(), e.getMessage());
            job.setJobEvent(JobEvent.ERRORED);
            job.setErrorMessage(e.getMessage());
            this.databaseRefManager.deleteServiceKey(job);
            jobRepo.save(job);
            return new AsyncResult<Boolean>(false);
        }

        job.setJobEvent(JobEvent.FINISHED);
        this.databaseRefManager.deleteServiceKey(job);
        jobRepo.save(job);
        return new AsyncResult<Boolean>(true);
    }
}
