package com.orange.clara.cloud.model;

/**
 * Copyright (C) 2015 Arthur Halet
 * <p/>
 * This software is distributed under the terms and conditions of the 'MIT'
 * license which can be found in the file 'LICENSE' in this package distribution
 * or at 'http://opensource.org/licenses/MIT'.
 * <p/>
 * Author: Arthur Halet
 * Date: 08/09/2015
 */
public enum DatabaseType {
    MYSQL(".*(maria|my).*"), POSTGRESQL(".*(postgres|pgsql).*"), MONGODB(".*mongo.*");

    private String matcher;

    DatabaseType(String matcher) {
        this.matcher = matcher;
    }


    public String getMatcher() {
        return matcher;
    }
}
