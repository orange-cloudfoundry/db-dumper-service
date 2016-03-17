package com.orange.clara.cloud.servicedbdumper.security.useraccess;

import com.orange.clara.cloud.servicedbdumper.exception.UserAccessRightException;
import com.orange.clara.cloud.servicedbdumper.model.DatabaseRef;
import com.orange.clara.cloud.servicedbdumper.model.DbDumperServiceInstance;
import org.cloudfoundry.client.lib.CloudFoundryClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Collection;

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

    public final static String AUTHORIZED_AUTHORITY = "ADMIN";
    protected SecurityContext securityContext = SecurityContextHolder.getContext();
    @Autowired()
    @Qualifier("cloudFoundryClientAsUser")
    private CloudFoundryClient cloudFoundryClient;

    @Override
    public Boolean haveAccessToServiceInstance(String serviceInstanceId) throws UserAccessRightException {
        SecurityContext context = securityContext;
        if (context == null) {
            return this.cloudFoundryClient.checkUserPermission(serviceInstanceId);
        }

        Authentication authentication = context.getAuthentication();
        if (authentication == null) {
            return this.cloudFoundryClient.checkUserPermission(serviceInstanceId);
        }

        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();

        if (authorities.contains(new SimpleGrantedAuthority(AUTHORIZED_AUTHORITY))) {
            return true;
        }
        return this.cloudFoundryClient.checkUserPermission(serviceInstanceId);
    }

    @Override
    public Boolean haveAccessToServiceInstance(DbDumperServiceInstance dbDumperServiceInstance) throws UserAccessRightException {
        return this.haveAccessToServiceInstance(dbDumperServiceInstance.getServiceInstanceId());
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

}
