package com.orange.clara.cloud.servicedbdumper.exception;

/**
 * Copyright (C) 2015 Orange
 * <p/>
 * This software is distributed under the terms and conditions of the 'Apache-2.0'
 * license which can be found in the file 'LICENSE' in this package distribution
 * or at 'https://opensource.org/licenses/Apache-2.0'.
 * <p/>
 * Author: Arthur Halet
 * Date: 27/10/2015
 */
public class DumpException extends Exception {

    public DumpException(String message) {
        super(message);
    }

    public DumpException(String message, Throwable cause) {
        super(message, cause);
    }
}
