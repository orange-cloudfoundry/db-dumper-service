package com.orange.clara.cloud.servicedbdumper.dbdumper.core;

import com.orange.clara.cloud.servicedbdumper.dbdumper.Dumper;
import com.orange.clara.cloud.servicedbdumper.dbdumper.core.dbdrivers.DatabaseDriver;
import com.orange.clara.cloud.servicedbdumper.exception.DumpException;
import com.orange.clara.cloud.servicedbdumper.exception.RunProcessException;
import com.orange.clara.cloud.servicedbdumper.model.DatabaseDumpFile;
import com.orange.clara.cloud.servicedbdumper.model.DatabaseRef;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

/**
 * Copyright (C) 2015 Orange
 * <p>
 * This software is distributed under the terms and conditions of the 'Apache-2.0'
 * license which can be found in the file 'LICENSE' in this package distribution
 * or at 'https://opensource.org/licenses/Apache-2.0'.
 * <p>
 * Author: Arthur Halet
 * Date: 09/09/2015
 */
public class CoreDumper extends AbstractCoreDbAction implements Dumper {

    private Logger logger = LoggerFactory.getLogger(CoreDumper.class);


    @Override
    @Transactional
    public void dump(DatabaseRef databaseRef) throws DumpException {
        try {
            DatabaseDriver databaseDriver = dbDumpersFactory.getDatabaseDumper(databaseRef);
            String fileName = this.generateFileName(databaseDriver);
            logger.info("Dumping database '" + databaseRef.getName() + "' with " + databaseRef.getType() + " binary ...");
            this.runDump(databaseDriver, databaseRef.getName() + "/" + fileName);
            this.createDatabaseDumpFile(databaseRef, fileName, databaseDriver.isDumpShowable());
        } catch (Exception e) {
            this.logOutputFromProcess();
            throw new DumpException("\nAn error occurred: " + e.getMessage() + this.getErrorMessageFromProcess(), e);
        }
        logger.info("Dumping database '" + databaseRef.getName() + "' with " + databaseRef.getType() + " binary finished.");
    }

    private void runDump(DatabaseDriver databaseDriver, String fileName) throws IOException, InterruptedException, RunProcessException {
        String[] commandLine = databaseDriver.getDumpCommandLine();
        int i = 1;
        while (true) {
            Process p = this.runCommandLine(commandLine);
            try {
                this.filer.store(p.getInputStream(), fileName);
            } catch (IOException e) {
            } catch (Exception e) {
                if (p.isAlive()) {
                    p.destroy();
                }
                throw e;
            }
            p.waitFor();
            if (p.exitValue() == 0) {
                break;
            }
            if (i >= this.dbCommandRetry) {
                this.filer.delete(fileName);
                throw new RunProcessException("\nError during process (exit code is " + p.exitValue() + "): ");
            }
            logger.warn("Retry {}/{}: fail to dump data for file {}.", i, dbCommandRetry, fileName);
            i++;
        }
    }

    private void createDatabaseDumpFile(DatabaseRef databaseRef, String fileName, boolean isDumpShowable) {
        SimpleDateFormat form = new SimpleDateFormat(this.dateFormat);
        Date today = new Date();
        try {
            today = form.parse(form.format(new Date()));
        } catch (ParseException e) { // should have no error
        }
        if (this.databaseDumpFileRepo.findByDatabaseRefAndCreatedAt(databaseRef, today) == null) {
            String user = this.generateUser();
            String password = this.generatePassword();
            this.databaseDumpFileRepo.save(new DatabaseDumpFile(fileName, databaseRef, user, password, isDumpShowable,
                    this.filer.getContentLength(databaseRef.getName() + "/" + fileName)));
        }
    }

    private String generateUser() {
        return UUID.randomUUID().toString();
    }

    private String generatePassword() {
        return UUID.randomUUID().toString();
    }
}
