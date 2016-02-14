package com.orange.clara.cloud.servicedbdumper.dbdumper.running.core;

import com.orange.clara.cloud.servicedbdumper.dbdumper.DatabaseDumper;
import com.orange.clara.cloud.servicedbdumper.dbdumper.running.Dumper;
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
 * <p/>
 * This software is distributed under the terms and conditions of the 'MIT'
 * license which can be found in the file 'LICENSE' in this package distribution
 * or at 'http://opensource.org/licenses/MIT'.
 * <p/>
 * Author: Arthur Halet
 * Date: 09/09/2015
 */
public class CoreDumper extends AbstractCoreDbAction implements Dumper {

    private Logger logger = LoggerFactory.getLogger(CoreDumper.class);


    @Override
    @Transactional
    public void dump(DatabaseRef databaseRef) throws DumpException {
        try {
            DatabaseDumper databaseDumper = dbDumpersFactory.getDatabaseDumper(databaseRef);
            String fileName = this.generateFileName(databaseDumper);
            logger.info("Dumping database '" + databaseRef.getName() + "' with " + databaseRef.getType() + " binary ...");
            this.runDump(databaseDumper, databaseRef.getName() + "/" + fileName);
            this.createDatabaseDumpFile(databaseRef, fileName, databaseDumper.isDumpShowable());
        } catch (Exception e) {
            this.logOutputFromProcess();
            throw new DumpException("\nAn error occurred: " + e.getMessage() + this.getErrorMessageFromProcess(), e);
        }
        logger.info("Dumping database '" + databaseRef.getName() + "' with " + databaseRef.getType() + " binary finished.");
    }

    private void runDump(DatabaseDumper databaseDumper, String fileName) throws IOException, InterruptedException, RunProcessException {
        String[] commandLine = databaseDumper.getDumpCommandLine();
        Process p = this.runCommandLine(commandLine);
        this.filer.store(p.getInputStream(), fileName);
        if (p.exitValue() != 0) {
            this.filer.delete(fileName);
            throw new RunProcessException("\nError during process (exit code is " + p.exitValue() + "): ");
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
            this.databaseDumpFileRepo.save(new DatabaseDumpFile(fileName, databaseRef, user, password, isDumpShowable));
        }
    }

    private String generateUser() {
        return UUID.randomUUID().toString();
    }

    private String generatePassword() {
        return UUID.randomUUID().toString();
    }
}
