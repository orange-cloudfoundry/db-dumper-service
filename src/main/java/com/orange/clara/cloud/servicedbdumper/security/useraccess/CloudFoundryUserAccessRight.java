package com.orange.clara.cloud.servicedbdumper.security.useraccess;

import com.orange.clara.cloud.servicedbdumper.exception.UserAccessRightException;
import com.orange.clara.cloud.servicedbdumper.model.DatabaseRef;
import com.orange.clara.cloud.servicedbdumper.model.DbDumperServiceInstance;
import com.orange.clara.cloud.servicedbdumper.security.AccessManager;
import org.cloudfoundry.client.lib.CloudFoundryClient;
import org.cloudfoundry.client.lib.CloudFoundryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

/**
 * Copyright (C) 2016 Orange
 * <p>
 * This software is distributed under the terms and conditions of the 'Apache-2.0'
 * license which can be found in the file 'LICENSE' in this package distribution
 * or at 'https://opensource.org/licenses/Apache-2.0'.
 * <p>
 * Author: Arthur Halet
 * Date: 11/02/2016
 */
public class CloudFoundryUserAccessRight implements UserAccessRight {

    @Autowired
    protected AccessManager accessManager;
    @Autowired()
    @Qualifier("cloudFoundryClientAsUser")
    private CloudFoundryClient cloudFoundryClient;
    private Logger logger = LoggerFactory.getLogger(CloudFoundryUserAccessRight.class);

    @Override
    public Boolean haveAccessToServiceInstance(String serviceInstanceId) throws UserAccessRightException {
        if (accessManager.isUserIsAdmin()) {
            return true;
        }
        try {
            return this.cloudFoundryClient.checkUserPermission(serviceInstanceId);
        } catch (CloudFoundryException e) {
            logger.error("Error during checking permission for {}, with error code {} and message {}",
                    serviceInstanceId, e.getCloudFoundryErrorCode(), e.getDescription());
            return false;
        }

    }

    @Override
    public Boolean haveAccessToServiceInstance(DatabaseRef databaseRef) throws UserAccessRightException {
        for (DbDumperServiceInstance dbDumperServiceInstance : databaseRef.getDbDumperServiceInstances()) {
            if (this.haveAccessToServiceInstance(dbDumperServiceInstance)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public Boolean haveAccessToServiceInstance(DbDumperServiceInstance dbDumperServiceInstance) throws UserAccessRightException {
        return this.haveAccessToServiceInstance(dbDumperServiceInstance.getServiceInstanceId());
    }

}
