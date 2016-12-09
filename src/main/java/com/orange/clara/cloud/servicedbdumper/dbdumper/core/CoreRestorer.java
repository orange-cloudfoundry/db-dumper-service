package com.orange.clara.cloud.servicedbdumper.dbdumper.core;

import com.orange.clara.cloud.servicedbdumper.dbdumper.Restorer;
import com.orange.clara.cloud.servicedbdumper.dbdumper.core.dbdrivers.DatabaseDriver;
import com.orange.clara.cloud.servicedbdumper.exception.CannotFindDatabaseDumperException;
import com.orange.clara.cloud.servicedbdumper.exception.RestoreCannotFindFileException;
import com.orange.clara.cloud.servicedbdumper.exception.RestoreException;
import com.orange.clara.cloud.servicedbdumper.exception.RunProcessException;
import com.orange.clara.cloud.servicedbdumper.model.DatabaseDumpFile;
import com.orange.clara.cloud.servicedbdumper.model.DatabaseRef;
import com.orange.clara.cloud.servicedbdumper.model.DbDumperServiceInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.Date;

/**
 * Copyright (C) 2015 Orange
 * <p>
 * This software is distributed under the terms and conditions of the 'Apache-2.0'
 * license which can be found in the file 'LICENSE' in this package distribution
 * or at 'https://opensource.org/licenses/Apache-2.0'.
 * <p>
 * Author: Arthur Halet
 * Date: 01/10/2015
 */
public class CoreRestorer extends AbstractCoreDbAction implements Restorer {


    private Logger logger = LoggerFactory.getLogger(CoreRestorer.class);

    @Override
    @Transactional
    public void restore(DbDumperServiceInstance dbDumperServiceInstance, DatabaseRef databaseRefTarget, Date date) throws RestoreException {
        DatabaseRef databaseRefSource = dbDumperServiceInstance.getDatabaseRef();
        logger.info("Restoring dump file from " + databaseRefSource.getName() + " to " + databaseRefTarget.getName() + " ...");
        DatabaseDumpFile dumpFile = this.findDumpFile(dbDumperServiceInstance, date);
        if (dumpFile == null) {
            throw new RestoreCannotFindFileException(databaseRefSource);
        }
        String fileName = this.getFileName(dumpFile);
        try {
            DatabaseDriver databaseDriver = findAndCheckDatabaseDumper(databaseRefSource, databaseRefTarget);
            this.runRestore(databaseDriver, fileName);
        } catch (Exception e) {
            this.logOutputFromProcess();
            throw new RestoreException("\nAn error occurred (see before for process errors): " + e.getMessage(), e);
        }
        logger.info("Restoring dump file from " + databaseRefSource.getName() + " to " + databaseRefTarget.getName() + " finished.");
    }

    @Override
    public void restore(DbDumperServiceInstance dbDumperServiceInstance, DatabaseRef databaseRefTarget) throws RestoreException {
        this.restore(dbDumperServiceInstance, databaseRefTarget, null);
    }


    protected void runRestore(DatabaseDriver databaseDriver, String fileName) throws IOException, InterruptedException, RunProcessException {
        int i = 1;
        while (true) {
            Process p = this.runCommandLine(databaseDriver.getRestoreCommandLine(), true);
            this.filer.retrieve(p.getOutputStream(), fileName);
            p.waitFor();
            if (p.exitValue() == 0) {
                break;
            }
            if (i >= this.dbCommandRetry) {
                throw new RunProcessException("\nError during process (exit code is " + p.exitValue() + "): ");
            }
            logger.warn("Retry {}/{}: fail to restore data for file {}.", i, dbCommandRetry, fileName);
            Thread.sleep(10000);
            i++;
        }
    }

    protected DatabaseDriver findAndCheckDatabaseDumper(DatabaseRef databaseRefSource, DatabaseRef databaseRefTarget) throws CannotFindDatabaseDumperException, RestoreException {
        if (!databaseRefSource.getType().equals(databaseRefTarget.getType())) {
            throw new RestoreException("Database " + databaseRefTarget.getName() + " should be a " + databaseRefSource.getType() + " database");
        }
        return dbDumpersFactory.getDatabaseDumper(databaseRefTarget);
    }

    private DatabaseDumpFile findDumpFile(DbDumperServiceInstance dbDumperServiceInstance, Date date) {
        if (date == null) {
            return this.databaseDumpFileRepo.findFirstByDbDumperServiceInstanceOrderByCreatedAtDesc(dbDumperServiceInstance);
        }
        return this.databaseDumpFileRepo.findFirstByDbDumperServiceInstanceAndCreatedAtGreaterThanEqualOrderByCreatedAtAsc(dbDumperServiceInstance, date);
    }
}
