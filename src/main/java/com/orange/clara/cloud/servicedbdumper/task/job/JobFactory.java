package com.orange.clara.cloud.servicedbdumper.task.job;

import com.orange.clara.cloud.servicedbdumper.exception.JobAlreadyExist;
import com.orange.clara.cloud.servicedbdumper.model.DatabaseRef;
import com.orange.clara.cloud.servicedbdumper.model.Job;
import com.orange.clara.cloud.servicedbdumper.model.JobEvent;
import com.orange.clara.cloud.servicedbdumper.model.JobType;
import com.orange.clara.cloud.servicedbdumper.repo.JobRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

/**
 * Copyright (C) 2015 Orange
 * <p/>
 * This software is distributed under the terms and conditions of the 'MIT'
 * license which can be found in the file 'LICENSE' in this package distribution
 * or at 'http://opensource.org/licenses/MIT'.
 * <p/>
 * Author: Arthur Halet
 * Date: 27/11/2015
 */
public class JobFactory {
    @Autowired
    private JobRepo jobRepo;

    @Transactional
    public void createJob(JobType jobType, DatabaseRef databaseRefSrc, DatabaseRef databaseRefTarget, Date updatedAt) throws JobAlreadyExist {
        if (this.jobRepo.findByJobTypeAndJobEventAndDatabaseRefSrcAndDatabaseRefTarget(jobType, JobEvent.START, databaseRefSrc, databaseRefTarget).size() > 0) {
            throw new JobAlreadyExist(new Job(jobType, databaseRefSrc));
        }
        if (this.jobRepo.findByJobTypeAndJobEventAndDatabaseRefSrcAndDatabaseRefTarget(jobType, JobEvent.RUNNING, databaseRefSrc, databaseRefTarget).size() > 0) {
            throw new JobAlreadyExist(new Job(jobType, databaseRefSrc));
        }
        this.jobRepo.save(new Job(jobType, databaseRefSrc, databaseRefTarget, updatedAt));
    }

    @Transactional
    public void createJobWithDatabaseRefSrc(JobType jobType, DatabaseRef databaseRefSrc) throws JobAlreadyExist {
        if (this.jobRepo.findByJobTypeAndJobEventAndDatabaseRefSrc(jobType, JobEvent.START, databaseRefSrc).size() > 0) {
            throw new JobAlreadyExist(new Job(jobType, databaseRefSrc));
        }
        if (this.jobRepo.findByJobTypeAndJobEventAndDatabaseRefSrc(jobType, JobEvent.RUNNING, databaseRefSrc).size() > 0) {
            throw new JobAlreadyExist(new Job(jobType, databaseRefSrc));
        }
        this.jobRepo.save(new Job(jobType, databaseRefSrc));
    }


    public void createJobCreateDump(DatabaseRef databaseRefSrc) throws JobAlreadyExist {
        this.createJobWithDatabaseRefSrc(JobType.CREATE_DUMP, databaseRefSrc);
    }

    public void createJobDeleteDumps(DatabaseRef databaseRefSrc) throws JobAlreadyExist {
        this.createJobWithDatabaseRefSrc(JobType.DELETE_DUMPS, databaseRefSrc);
    }

    public void createJobDeleteDatabaseRef(DatabaseRef databaseRefSrc) throws JobAlreadyExist {
        this.createJobWithDatabaseRefSrc(JobType.DELETE_DATABASE_REF, databaseRefSrc);
    }

    public void createJobRestoreDump(DatabaseRef databaseRefSrc, DatabaseRef databaseRefTarget, Date createdAt) throws JobAlreadyExist {
        this.createJob(JobType.RESTORE_DUMP, databaseRefSrc, databaseRefTarget, createdAt);
    }

    @Transactional
    public void purgeJob() {
        this.jobRepo.deleteByJobEvent(JobEvent.ERRORED);
        this.purgeFinishedJob();
    }

    @Transactional
    public void purgeFinishedJob() {
        this.jobRepo.deleteByJobEvent(JobEvent.FINISHED);
    }
}
