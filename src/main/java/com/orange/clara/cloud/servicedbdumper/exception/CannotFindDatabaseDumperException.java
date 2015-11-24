package com.orange.clara.cloud.servicedbdumper.exception;

import com.orange.clara.cloud.servicedbdumper.model.DatabaseRef;

/**
 * Copyright (C) 2015 Orange
 * <p/>
 * This software is distributed under the terms and conditions of the 'MIT'
 * license which can be found in the file 'LICENSE' in this package distribution
 * or at 'http://opensource.org/licenses/MIT'.
 * <p/>
 * Author: Arthur Halet
 * Date: 27/10/2015
 */
public class CannotFindDatabaseDumperException extends Exception {
    public CannotFindDatabaseDumperException(DatabaseRef databaseRef) {
        super("cannot find database dumper for " + databaseRef.getName() + " and type " + databaseRef.getType());
    }

    public CannotFindDatabaseDumperException(String message) {
        super(message);
    }

    public CannotFindDatabaseDumperException(String message, Throwable cause) {
        super(message, cause);
    }
}
