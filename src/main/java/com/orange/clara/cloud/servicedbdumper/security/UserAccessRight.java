package com.orange.clara.cloud.servicedbdumper.security;

import com.orange.clara.cloud.servicedbdumper.exception.UserAccessRightException;
import com.orange.clara.cloud.servicedbdumper.model.DbDumperServiceInstance;

import java.util.List;

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
public interface UserAccessRight {

    Boolean haveAccessToServiceInstance(String serviceInstanceId) throws UserAccessRightException;

    Boolean haveAccessToServiceInstance(List<DbDumperServiceInstance> dbDumperServiceInstances) throws UserAccessRightException;

    Boolean haveAccessToServiceInstance(DbDumperServiceInstance dbDumperServiceInstance) throws UserAccessRightException;
}
