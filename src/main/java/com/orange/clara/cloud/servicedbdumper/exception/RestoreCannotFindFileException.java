package com.orange.clara.cloud.servicedbdumper.exception;

import com.orange.clara.cloud.servicedbdumper.model.DatabaseRef;

/**
 * Copyright (C) 2015 Orange
 * <p>
 * This software is distributed under the terms and conditions of the 'Apache-2.0'
 * license which can be found in the file 'LICENSE' in this package distribution
 * or at 'https://opensource.org/licenses/Apache-2.0'.
 * <p>
 * Author: Arthur Halet
 * Date: 13/10/2015
 */
public class RestoreCannotFindFileException extends RestoreException {

    public RestoreCannotFindFileException(DatabaseRef databaseRef) {
        super("cannot restore because we can't find dump associated to " + databaseRef.getName());
    }
}
