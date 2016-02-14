package com.orange.clara.cloud.servicedbdumper.exception;

/**
 * Copyright (C) 2016 Orange
 * <p/>
 * This software is distributed under the terms and conditions of the 'Apache-2.0'
 * license which can be found in the file 'LICENSE' in this package distribution
 * or at 'https://opensource.org/licenses/Apache-2.0'.
 * <p/>
 * Author: Arthur Halet
 * Date: 11/02/2016
 */
public class UserAccessRightException extends Exception {

    public UserAccessRightException(String message) {
        super(message);
    }

    public UserAccessRightException(String message, Throwable cause) {
        super(message, cause);
    }
}