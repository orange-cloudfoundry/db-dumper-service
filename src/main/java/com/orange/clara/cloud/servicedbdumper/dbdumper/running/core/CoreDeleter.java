package com.orange.clara.cloud.servicedbdumper.dbdumper.running.core;

import com.orange.clara.cloud.servicedbdumper.dbdumper.running.Deleter;
import com.orange.clara.cloud.servicedbdumper.model.DatabaseDumpFile;
import com.orange.clara.cloud.servicedbdumper.model.DatabaseRef;
import com.orange.clara.cloud.servicedbdumper.repo.DatabaseDumpFileRepo;
import com.orange.clara.cloud.servicedbdumper.repo.DatabaseRefRepo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

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
public class CoreDeleter extends AbstractCoreDbAction implements Deleter {

    @Autowired
    DatabaseDumpFileRepo databaseDumpFileRepo;

    @Autowired
    DatabaseRefRepo databaseRefRepo;
    private Logger logger = LoggerFactory.getLogger(CoreDeleter.class);

    @Override
    public void deleteAll(DatabaseRef databaseRef) {
        String fileName = "";
        for (DatabaseDumpFile databaseDumpFile : databaseRef.getDatabaseDumpFiles()) {
            this.delete(databaseDumpFile);
        }
    }

    @Override
    public void delete(DatabaseDumpFile databaseDumpFile) {
        String fileName = this.getFileName(databaseDumpFile);
        DatabaseRef databaseRef = databaseDumpFile.getDatabaseRef();
        logger.info(String.format("Deleting file '%s' from s3", fileName));
        this.filer.delete(fileName);
        databaseRef.removeDatabaseDumpFile(databaseDumpFile);
        databaseRefRepo.save(databaseRef);
        this.databaseDumpFileRepo.delete(databaseDumpFile);
    }
}
