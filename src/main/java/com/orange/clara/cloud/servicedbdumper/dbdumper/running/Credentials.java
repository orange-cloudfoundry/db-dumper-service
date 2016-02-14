package com.orange.clara.cloud.servicedbdumper.dbdumper.running;

import com.orange.clara.cloud.servicedbdumper.model.DbDumperServiceInstance;

import java.util.Map;

/**
 * Copyright (C) 2015 Arthur Halet
 * <p/>
 * This software is distributed under the terms and conditions of the 'MIT'
 * license which can be found in the file 'LICENSE' in this package distribution
 * or at 'http://opensource.org/licenses/MIT'.
 * <p/>
 * Author: Arthur Halet
 * Date: 26/11/2015
 */
public interface Credentials {
    Map<String, Object> getCredentials(DbDumperServiceInstance dbDumperServiceInstance);
}
