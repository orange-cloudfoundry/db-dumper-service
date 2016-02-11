package com.orange.clara.cloud.servicedbdumper.exception;

/**
 * Copyright (C) 2016 Arthur Halet
 * <p/>
 * This software is distributed under the terms and conditions of the 'MIT'
 * license which can be found in the file 'LICENSE' in this package distribution
 * or at 'http://opensource.org/licenses/MIT'.
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