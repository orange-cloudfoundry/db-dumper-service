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
public class PostgresqlDatabaseDumper extends AbstractDatabaseDumper implements DatabaseDumper {
    public PostgresqlDatabaseDumper(File binaryDump, File binaryRestore) {
        super(binaryDump, binaryRestore);
    }


    @Override
    public String[] getDumpCommandLine() {
        return String.format(
                "%s --dbname=postgresql://%s:%s@%s:%s/%s",
                this.binaryDump.getAbsolutePath(),
                this.databaseRef.getUser(),
                this.databaseRef.getPassword(),
                this.databaseRef.getHost(),
                this.databaseRef.getPort(),
                this.databaseRef.getDatabaseName()
        ).split(" ");
    }

    @Override
    public String[] getRestoreCommandLine() {
        return String.format(
                "%s --dbname=postgresql://%s:%s@%s:%s/%s -f",
                this.binaryRestore.getAbsolutePath(),
                this.databaseRef.getUser(),
                this.databaseRef.getPassword(),
                this.databaseRef.getHost(),
                this.databaseRef.getPort(),
                this.databaseRef.getDatabaseName()
        ).split(" ");
    }
}
