package com.orange.clara.cloud.servicedbdumper.dbdumper.running;

import com.orange.clara.cloud.servicedbdumper.exception.RestoreException;
import com.orange.clara.cloud.servicedbdumper.model.DatabaseRef;

import java.util.Date;

/**
 * Copyright (C) 2015 Arthur Halet
 * <p/>
 * This software is distributed under the terms and conditions of the 'MIT'
 * license which can be found in the file 'LICENSE' in this package distribution
 * or at 'http://opensource.org/licenses/MIT'.
 * <p/>
 * Author: Arthur Halet
 * Date: 24/11/2015
 */
public interface Restorer {
    void restore(DatabaseRef databaseRefSource, DatabaseRef databaseRefTarget, Date date) throws RestoreException;

    void restore(DatabaseRef databaseRefSource, DatabaseRef databaseRefTarget) throws RestoreException;
}
