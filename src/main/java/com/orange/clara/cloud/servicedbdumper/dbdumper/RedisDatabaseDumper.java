package com.orange.clara.cloud.servicedbdumper.dbdumper;

import java.io.File;

/**
 * Copyright (C) 2016 Orange
 * <p/>
 * This software is distributed under the terms and conditions of the 'Apache-2.0'
 * license which can be found in the file 'LICENSE' in this package distribution
 * or at 'https://opensource.org/licenses/Apache-2.0'.
 * <p/>
 * Author: Arthur Halet
 * Date: 14/02/2016
 */
public class RedisDatabaseDumper extends AbstractDatabaseDumper implements DatabaseDumper {

    public RedisDatabaseDumper(File rutilBinary) {
        super(rutilBinary, rutilBinary);
    }

    @Override
    public String[] getDumpCommandLine() {
        return String.format("%s restore --host=%s --port=%s --auth=%s -o",
                this.binaryRestore.getAbsolutePath(),
                this.databaseRef.getHost(),
                this.databaseRef.getPort(),
                this.getPassword()
        ).split(" ");
    }

    @Override
    public String[] getRestoreCommandLine() {
        return String.format("%s restore --host=%s --port=%s --auth=%s -i",
                this.binaryRestore.getAbsolutePath(),
                this.databaseRef.getHost(),
                this.databaseRef.getPort(),
                this.getPassword()
        ).split(" ");
    }

    private String getPassword() {
        String password = "";
        if (!this.databaseRef.getUser().isEmpty()) {
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
