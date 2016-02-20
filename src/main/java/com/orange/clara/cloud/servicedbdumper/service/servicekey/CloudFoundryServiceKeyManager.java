package com.orange.clara.cloud.servicedbdumper.service.servicekey;

import com.orange.clara.cloud.servicedbdumper.exception.ServiceKeyException;
import com.orange.clara.cloud.servicedbdumper.model.DatabaseService;
import org.cloudfoundry.client.lib.CloudCredentials;
import org.cloudfoundry.client.lib.CloudFoundryClient;
import org.cloudfoundry.client.lib.domain.CloudService;
import org.cloudfoundry.client.lib.domain.CloudServiceKey;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.oauth2.common.DefaultOAuth2AccessToken;

import java.util.UUID;

/**
 * Copyright (C) 2016 Orange
 * <p>
 * This software is distributed under the terms and conditions of the 'Apache-2.0'
 * license which can be found in the file 'LICENSE' in this package distribution
 * or at 'https://opensource.org/licenses/Apache-2.0'.
 * <p>
 * Author: Arthur Halet
 * Date: 19/02/2016
 */
public class CloudFoundryServiceKeyManager implements ServiceKeyManager {

    @Autowired
    @Qualifier("cloudFoundryClientAsAdmin")
    private CloudFoundryClient cloudFoundryClient;

    @Override
    public CloudServiceKey createServiceKey(String serviceName, String token, String org, String space) throws ServiceKeyException {
        CloudService cloudService = this.getUserService(serviceName, token, org, space);
        if (cloudService == null) {
            throw new ServiceKeyException("User don't have access to service '" + serviceName + "'");
        }
        return this.cloudFoundryClient.createServiceKey(cloudService, UUID.randomUUID().toString());
    }

    @Override
    public CloudServiceKey createServiceKey(DatabaseService databaseService) throws ServiceKeyException {
        return this.cloudFoundryClient.createServiceKey(databaseService.getUuid(), UUID.randomUUID().toString());
    }

    @Override
    public void deleteServiceKey(String guid) {
        this.cloudFoundryClient.deleteServiceKey(guid);
    }

    @Override
    public void deleteServiceKey(DatabaseService databaseService) {
        this.deleteServiceKey(databaseService.getServiceKeyGuid());
    }

    public CloudService getUserService(String serviceName, String token, String org, String space) {
        CloudCredentials cloudCredentials = new CloudCredentials(new DefaultOAuth2AccessToken(token), false);
        CloudFoundryClient cloudFoundryClientUser;
        if (org != null && space != null) {
            cloudFoundryClientUser = new CloudFoundryClient(cloudCredentials, this.cloudFoundryClient.getCloudControllerUrl(), org, space);
        } else {
            cloudFoundryClientUser = new CloudFoundryClient(cloudCredentials, this.cloudFoundryClient.getCloudControllerUrl());
        }
        return cloudFoundryClientUser.getService(serviceName);
    }
}
