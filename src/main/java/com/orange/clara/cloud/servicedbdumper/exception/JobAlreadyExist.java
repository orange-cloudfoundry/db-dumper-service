package com.orange.clara.cloud.servicedbdumper.exception;

import com.orange.clara.cloud.servicedbdumper.model.Job;

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
public class JobAlreadyExist extends Exception {
    public JobAlreadyExist(Job job) {
        super(String.format("Job type '%s' already exist for database '%s'", job.getJobType(), job.getDatabaseRefSrc().getName()));
    }

    public JobAlreadyExist(String message) {
        super(message);
    }

    public JobAlreadyExist(String message, Throwable cause) {
        super(message, cause);
    }
}
