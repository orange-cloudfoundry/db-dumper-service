package com.orange.clara.cloud.servicedbdumper.security;

import com.orange.clara.cloud.servicedbdumper.exception.UserAccessRightException;
import com.orange.clara.cloud.servicedbdumper.model.DbDumperServiceInstance;

import java.util.List;

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
public class DefaultUserAccessRight implements UserAccessRight {
    @Override
    public Boolean haveAccessToServiceInstance(String serviceInstanceId) {
        return true;
    }

    @Override
    public Boolean haveAccessToServiceInstance(List<DbDumperServiceInstance> dbDumperServiceInstances) throws UserAccessRightException {
        return true;
    }

    @Override
    public Boolean haveAccessToServiceInstance(DbDumperServiceInstance dbDumperServiceInstance) throws UserAccessRightException {
        return true;
    }
}
