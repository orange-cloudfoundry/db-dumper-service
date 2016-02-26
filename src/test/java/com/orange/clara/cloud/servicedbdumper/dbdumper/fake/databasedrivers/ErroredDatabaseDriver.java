package com.orange.clara.cloud.servicedbdumper.dbdumper.fake.databasedrivers;

import com.orange.clara.cloud.servicedbdumper.dbdumper.core.dbdrivers.AbstractDatabaseDriver;
import com.orange.clara.cloud.servicedbdumper.dbdumper.core.dbdrivers.DatabaseDriver;

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
public class ErroredDatabaseDriver extends AbstractDatabaseDriver implements DatabaseDriver {

    public ErroredDatabaseDriver() {
        super(null, null);
    }

    @Override
    public String[] getDumpCommandLine() {
        return new String[]{
                "cat",
                "1"
        };
    }

    @Override
    public String[] getRestoreCommandLine() {
        return new String[]{
                "cat",
                "1"
        };
    }

    @Override
    public Boolean isDumpShowable() {
        return true;
    }
}
