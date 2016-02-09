package com.orange.clara.cloud.servicedbdumper.model;

/**
 * Copyright (C) 2015 Orange
 * <p/>
 * This software is distributed under the terms and conditions of the 'MIT'
 * license which can be found in the file 'LICENSE' in this package distribution
 * or at 'http://opensource.org/licenses/MIT'.
 * <p/>
 * Author: Arthur Halet
 * Date: 08/09/2015
 */
public enum DatabaseType {
    MYSQL(".*(maria|my).*", 3306), POSTGRESQL(".*(postgres|pgsql).*", 5432), MONGODB(".*mongo.*", 27017);

    private String matcher;
    private Integer defaultPort;

    DatabaseType(String matcher, Integer defaultPort) {
        this.matcher = matcher;
        this.defaultPort = defaultPort;
    }

    public Integer getDefaultPort() {
        return defaultPort;
    }

    public String getMatcher() {
        return matcher;
    }
}
