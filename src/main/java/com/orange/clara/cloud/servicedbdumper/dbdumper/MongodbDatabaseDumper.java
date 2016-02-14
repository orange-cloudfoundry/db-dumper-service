package com.orange.clara.cloud.servicedbdumper.dbdumper;

import java.io.File;

/**
 * Copyright (C) 2015 Orange
 * <p/>
 * This software is distributed under the terms and conditions of the 'MIT'
 * license which can be found in the file 'LICENSE' in this package distribution
 * or at 'http://opensource.org/licenses/MIT'.
 * <p/>
 * Author: Arthur Halet
 * Date: 03/06/2015
 */
public class MongodbDatabaseDumper extends AbstractDatabaseDumper implements DatabaseDumper {
    public MongodbDatabaseDumper(File binaryDump, File binaryRestore) {
        super(binaryDump, binaryRestore);
    }

    @Override
    public String[] getDumpCommandLine() {
        return String.format(
                "%s --host %s --port %s --username %s --password %s --db %s --archive",
                this.binaryDump.getAbsolutePath(),
                this.databaseRef.getHost(),
                this.databaseRef.getPort(),
                this.databaseRef.getUser(),
                this.databaseRef.getPassword(),
                this.databaseRef.getDatabaseName()
        ).split(" ");
    }

    @Override
    public String[] getRestoreCommandLine() {
        return String.format(
                "%s --host %s --port %s --username %s --password %s --db %s --archive",
                this.binaryRestore.getAbsolutePath(),
                this.databaseRef.getHost(),
                this.databaseRef.getPort(),
                this.databaseRef.getUser(),
                this.databaseRef.getPassword(),
                this.databaseRef.getDatabaseName()
        ).split(" ");
    }

    @Override
    public String getFileExtension() {
        return ".bson";
    }
}
