package com.orange.clara.cloud.servicedbdumper.dbdumper.core;

import com.orange.clara.cloud.servicedbdumper.dbdumper.Deleter;
import com.orange.clara.cloud.servicedbdumper.model.DatabaseDumpFile;
import com.orange.clara.cloud.servicedbdumper.model.DbDumperServiceInstance;
import com.orange.clara.cloud.servicedbdumper.repo.DatabaseRefRepo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
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
public class CoreDeleter extends AbstractCoreDbAction implements Deleter {


    @Autowired
    private DatabaseRefRepo databaseRefRepo;
    private Logger logger = LoggerFactory.getLogger(CoreDeleter.class);

    @Override
    @Transactional
    public void deleteAll(DbDumperServiceInstance dbDumperServiceInstance) {
        List<DatabaseDumpFile> databaseDumpFileList = new ArrayList<>(dbDumperServiceInstance.getDatabaseDumpFiles());
        databaseDumpFileList.forEach(this::delete);
    }

    @Override
    @Transactional
    public void delete(DatabaseDumpFile databaseDumpFile) {
        String fileName = this.getFileName(databaseDumpFile);
        DbDumperServiceInstance dbDumperServiceInstance = databaseDumpFile.getDbDumperServiceInstance();
        this.filer.delete(fileName);
        logger.info(String.format("Delete file '%s' from s3", fileName));

        dbDumperServiceInstance.removeDatabaseDumpFile(databaseDumpFile);
        logger.info(String.format("Delete file '%s' from database_ref '%s'", fileName, dbDumperServiceInstance.getDatabaseRef().getDatabaseName()));

        serviceInstanceRepo.save(dbDumperServiceInstance);
        this.databaseDumpFileRepo.delete(databaseDumpFile);
        logger.info(String.format("Delete file '%s' from database", fileName));
    }
}
