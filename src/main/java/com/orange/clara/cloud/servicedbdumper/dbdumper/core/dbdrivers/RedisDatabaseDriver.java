package com.orange.clara.cloud.servicedbdumper.dbdumper.core.dbdrivers;

import java.io.File;

/**
 * Copyright (C) 2016 Orange
 * <p>
 * This software is distributed under the terms and conditions of the 'Apache-2.0'
 * license which can be found in the file 'LICENSE' in this package distribution
 * or at 'https://opensource.org/licenses/Apache-2.0'.
 * <p>
 * Author: Arthur Halet
 * Date: 14/02/2016
 */
public class RedisDatabaseDriver extends AbstractDatabaseDriver implements DatabaseDriver {

    public RedisDatabaseDriver(File rutilBinary) {
        super(rutilBinary, rutilBinary);
    }

    @Override
    public String[] getDumpCommandLine() {
        return String.format("%s dump -s %s -p %s -a %s -o",
                this.binaryRestore.getAbsolutePath(),
                this.databaseRef.getHost(),
                this.databaseRef.getPort(),
                this.getPassword()
        ).split(" ");
    }

    @Override
    public String[] getRestoreCommandLine() {
        return String.format("%s restore -s %s -p %s -a %s -i",
                this.binaryRestore.getAbsolutePath(),
                this.databaseRef.getHost(),
                this.databaseRef.getPort(),
                this.getPassword()
        ).split(" ");
    }

    @Override
    public Boolean isDumpShowable() {
        return false;
    }

    private String getPassword() {
        String password = "";
        if (!this.databaseRef.getUser().isEmpty() && this.databaseRef.getPassword().isEmpty()) {
            password = this.databaseRef.getUser();
        } else {
            password = this.databaseRef.getPassword();
        }
        return password;
    }

    @Override
    public String getFileExtension() {
        return ".rdmp";
    }
}
