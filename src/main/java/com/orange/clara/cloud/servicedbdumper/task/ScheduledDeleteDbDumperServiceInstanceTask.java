package com.orange.clara.cloud.servicedbdumper.task;

import com.orange.clara.cloud.servicedbdumper.exception.JobCreationException;
import com.orange.clara.cloud.servicedbdumper.model.*;
import com.orange.clara.cloud.servicedbdumper.repo.DatabaseRefRepo;
import com.orange.clara.cloud.servicedbdumper.repo.DatabaseServiceRepo;
import com.orange.clara.cloud.servicedbdumper.repo.DbDumperServiceInstanceRepo;
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
public class ScheduledDeleteDbDumperServiceInstanceTask {

    private Logger logger = LoggerFactory.getLogger(ScheduledDeleteDbDumperServiceInstanceTask.class);

    @Autowired
    private JobRepo jobRepo;

    @Autowired
    private DatabaseRefRepo databaseRefRepo;

    @Autowired
    private DatabaseServiceRepo databaseServiceRepo;

    @Autowired
    private DbDumperServiceInstanceRepo serviceInstanceRepo;

    @Autowired
    @Qualifier("jobFactory")
    private JobFactory jobFactory;

    @Scheduled(fixedDelay = 11000)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void deleteDbDumperServiceInstance() throws JobCreationException {
        List<Job> jobs = jobRepo.findByJobTypeAndJobEvent(JobType.DELETE_DB_DUMPER_SERVICE_INSTANCE, JobEvent.START);

        logger.debug("Running: delete database reference scheduled task ...");

        for (Job job : jobs) {
            job.setJobEvent(JobEvent.RUNNING);
            jobRepo.save(job);
            DbDumperServiceInstance dbDumperServiceInstance = job.getDbDumperServiceInstance();
            if (!dbDumperServiceInstance.isDeleted()) {
                continue;
            }
            if (dbDumperServiceInstance.getDatabaseDumpFiles().size() > 0) {
                this.jobFactory.createJobDeleteDumps(dbDumperServiceInstance.getDatabaseRef(), dbDumperServiceInstance);

                job.setJobEvent(JobEvent.FINISHED);
                jobRepo.save(job);
                continue;
            }
            job.setDatabaseRefSrc(null);
            jobRepo.save(job);
            try {
                DatabaseRef databaseRef = dbDumperServiceInstance.getDatabaseRef();
                databaseRef.removeDbDumperServiceInstance(dbDumperServiceInstance);
                this.databaseRefRepo.save(databaseRef);

                if (databaseRef.getDbDumperServiceInstances().size() == 0) {
                    this.deleteDatabaseRef(dbDumperServiceInstance.getDatabaseRef());
                }
                serviceInstanceRepo.delete(dbDumperServiceInstance);
            } catch (Exception e) {
                job.setJobEvent(JobEvent.ERRORED);
                job.setErrorMessage(e.getMessage());
                jobRepo.save(job);
                continue;
            }


            job.setJobEvent(JobEvent.FINISHED);
            jobRepo.save(job);
        }
        logger.debug("Finished: delete database reference scheduled task ...");

    }

    private void deleteDatabaseRef(DatabaseRef databaseRef) {
        if (databaseRef.getDatabaseService() != null) {
            databaseServiceRepo.delete(databaseRef.getDatabaseService());
        }
        databaseRefRepo.delete(databaseRef);
    }
}
