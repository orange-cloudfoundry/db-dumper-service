package com.orange.clara.cloud.servicedbdumper.dbdumper.core;

import com.orange.clara.cloud.servicedbdumper.exception.DatabaseExtractionException;
import com.orange.clara.cloud.servicedbdumper.model.DatabaseDumpFile;
import com.orange.clara.cloud.servicedbdumper.model.DatabaseRef;
import com.orange.clara.cloud.servicedbdumper.model.DbDumperServiceInstance;

import java.net.URI;

/**
 * Copyright (C) 2016 Orange
 * <p>
 * This software is distributed under the terms and conditions of the 'Apache-2.0'
 * license which can be found in the file 'LICENSE' in this package distribution
 * or at 'https://opensource.org/licenses/Apache-2.0'.
 * <p>
 * Author: Arthur Halet
 * Date: 26/02/2016
 */
public abstract class AbstractCoreTester {

    protected DatabaseRef generateDatabaseRef(String uri) throws DatabaseExtractionException {
        return new DatabaseRef("mydb", URI.create(uri));
    }

    protected DatabaseDumpFile generateDatabaseDumpFile(DbDumperServiceInstance dbDumperServiceInstance) throws DatabaseExtractionException {
        return new DatabaseDumpFile("myfile", dbDumperServiceInstance, "user", "password", true, 1);
    }

    protected DatabaseDumpFile generateDatabaseDumpFile(int id, DbDumperServiceInstance dbDumperServiceInstance) throws DatabaseExtractionException {
        DatabaseDumpFile databaseDumpFile = new DatabaseDumpFile("myfile" + id, dbDumperServiceInstance, "user", "password", true, 1);
        databaseDumpFile.setId(id);
        return databaseDumpFile;
    }

    protected DbDumperServiceInstance generateDbDumperServiceInstance(DatabaseRef databaseRef) {
        DbDumperServiceInstance dbDumperServiceInstance = new DbDumperServiceInstance();
        dbDumperServiceInstance.setDatabaseRef(databaseRef);
        return dbDumperServiceInstance;
    }
}
