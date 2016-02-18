package com.orange.clara.cloud.servicedbdumper.dbdumper.core.dbdrivers;

import java.io.File;

/**
 * Copyright (C) 2015 Orange
 * <p/>
 * This software is distributed under the terms and conditions of the 'Apache-2.0'
 * license which can be found in the file 'LICENSE' in this package distribution
 * or at 'https://opensource.org/licenses/Apache-2.0'.
 * <p/>
 * Author: Arthur Halet
 * Date: 03/06/2015
 */
public class PostgresqlDatabaseDriver extends AbstractDatabaseDriver implements DatabaseDriver {
    public PostgresqlDatabaseDriver(File binaryDump, File binaryRestore) {
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

    @Override
    public Boolean isDumpShowable() {
        return true;
    }
}
