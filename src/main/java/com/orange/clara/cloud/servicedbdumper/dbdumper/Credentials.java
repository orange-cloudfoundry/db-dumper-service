package com.orange.clara.cloud.servicedbdumper.dbdumper;

import com.orange.clara.cloud.servicedbdumper.model.DbDumperServiceInstance;

import java.util.Map;

/**
 * Copyright (C) 2015 Orange
 * <p/>
 * This software is distributed under the terms and conditions of the 'Apache-2.0'
 * license which can be found in the file 'LICENSE' in this package distribution
 * or at 'https://opensource.org/licenses/Apache-2.0'.
 * <p/>
 * Author: Arthur Halet
 * Date: 26/11/2015
 */
public interface Credentials {
    Map<String, Object> getCredentials(DbDumperServiceInstance dbDumperServiceInstance);
}
