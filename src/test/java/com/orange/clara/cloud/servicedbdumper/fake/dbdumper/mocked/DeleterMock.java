package com.orange.clara.cloud.servicedbdumper.fake.dbdumper.mocked;

import com.orange.clara.cloud.servicedbdumper.dbdumper.Deleter;
import com.orange.clara.cloud.servicedbdumper.model.DatabaseDumpFile;
import com.orange.clara.cloud.servicedbdumper.model.DbDumperServiceInstance;

/**
 * Copyright (C) 2016 Orange
 * <p>
 * This software is distributed under the terms and conditions of the 'Apache-2.0'
 * license which can be found in the file 'LICENSE' in this package distribution
 * or at 'https://opensource.org/licenses/Apache-2.0'.
 * <p>
 * Author: Arthur Halet
 * Date: 24/03/2016
 */
public class DeleterMock implements Deleter {

    private Integer numberCallDelete = 0;

    public Integer getNumberCallDelete() {
        return numberCallDelete;
    }

    public void resetNumberCallDelete() {
        numberCallDelete = 0;
    }

    @Override
    public void deleteAll(DbDumperServiceInstance dbDumperServiceInstance) {
        for (DatabaseDumpFile databaseDumpFile : dbDumperServiceInstance.getDatabaseDumpFiles()) {
            this.delete(databaseDumpFile);
        }
    }

    @Override
    public void delete(DatabaseDumpFile databaseDumpFile) {
        numberCallDelete++;
    }
}
