package com.orange.clara.cloud.servicedbdumper.dbdumper.core.dbdrivers;

import java.io.File;

/**
 * Copyright (C) 2015 Orange
 * <p>
 * This software is distributed under the terms and conditions of the 'Apache-2.0'
 * license which can be found in the file 'LICENSE' in this package distribution
 * or at 'https://opensource.org/licenses/Apache-2.0'.
 * <p>
 * Author: Arthur Halet
 * Date: 21/05/2015
 */
public class MysqlDatabaseDriver extends AbstractDatabaseDriver implements DatabaseDriver {
    public MysqlDatabaseDriver(File binaryDump, File binaryRestore) {
        super(binaryDump, binaryRestore);
    }


    @Override
    public String[] getDumpCommandLine() {
        return String.format("%s --routines --skip-add-locks --skip-extended-insert --single-transaction --add-drop-table --skip-comments --host=%s --port=%s --user=%s --password=%s %s",
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
        return new String[]{
                this.binaryRestore.getAbsolutePath(),
                "--host=" + this.databaseRef.getHost(),
                "--port=" + this.databaseRef.getPort(),
                "--user=" + this.databaseRef.getUser(),
                "--password=" + this.databaseRef.getPassword(),
                this.databaseRef.getDatabaseName()
        };
    }

    @Override
    public Boolean isDumpShowable() {
        return true;
    }
}
