package com.orange.clara.cloud.servicedbdumper.dbdumper.core;

import com.google.common.collect.Lists;
import com.orange.clara.cloud.servicedbdumper.dbdumper.Credentials;
import com.orange.clara.cloud.servicedbdumper.helper.UrlForge;
import com.orange.clara.cloud.servicedbdumper.model.DatabaseDumpFile;
import com.orange.clara.cloud.servicedbdumper.model.DatabaseRef;
import com.orange.clara.cloud.servicedbdumper.model.DbDumperCredential;
import com.orange.clara.cloud.servicedbdumper.model.DbDumperServiceInstance;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

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
public class CoreCredentials implements Credentials {

    @Autowired
    private UrlForge urlForge;

    @Override
    public List<DbDumperCredential> getDumpsCredentials(DbDumperServiceInstance dbDumperServiceInstance) {
        List<DbDumperCredential> dbDumperCredentials = Lists.newArrayList();
        List<DatabaseDumpFile> databaseDumpFiles = dbDumperServiceInstance.getDatabaseDumpFiles();
        for (DatabaseDumpFile databaseDumpFile : databaseDumpFiles) {
            dbDumperCredentials.add(new DbDumperCredential(
                    databaseDumpFile.getId(),
                    databaseDumpFile.getCreatedAt(),
                    urlForge.createDownloadLink(databaseDumpFile),
                    urlForge.createShowLink(databaseDumpFile),
                    databaseDumpFile.getFileName(),
                    databaseDumpFile.getSize(),
                    databaseDumpFile.getDeleted()
            ));
        }
        return dbDumperCredentials;
    }

    @Override
    public List<DbDumperCredential> getDumpsCredentials(DatabaseRef databaseRef) {
        List<DbDumperCredential> dbDumperCredentials = Lists.newArrayList();
        for (DbDumperServiceInstance dbDumperServiceInstance : databaseRef.getDbDumperServiceInstances()) {
            dbDumperCredentials.addAll(this.getDumpsCredentials(dbDumperServiceInstance));
        }
        return dbDumperCredentials;
    }
}
