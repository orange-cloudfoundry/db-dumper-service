package com.orange.clara.cloud.servicedbdumper.service.servicekey;

import com.orange.clara.cloud.servicedbdumper.cloudfoundry.CloudFoundryClientFactory;
import com.orange.clara.cloud.servicedbdumper.exception.ServiceKeyException;
import com.orange.clara.cloud.servicedbdumper.model.DatabaseService;
import org.cloudfoundry.client.lib.CloudFoundryClient;
import org.cloudfoundry.client.lib.domain.CloudService;
import org.cloudfoundry.client.lib.domain.CloudServiceKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

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
    private Logger logger = LoggerFactory.getLogger(CloudFoundryServiceKeyManager.class);
    @Autowired
    @Qualifier("cloudFoundryClientAsAdmin")
    private CloudFoundryClient cloudFoundryClient;

    @Autowired
    private CloudFoundryClientFactory cloudFoundryClientFactory;

    @Override
    public CloudServiceKey createServiceKey(String serviceName, String token, String org, String space) throws ServiceKeyException {
        logger.info("Creating service key for service {} ...", serviceName);
        CloudService cloudService = this.getUserService(serviceName, token, org, space);
        if (cloudService == null) {
            throw new ServiceKeyException("User don't have access to service '" + serviceName + "'");
        }
        CloudServiceKey cloudServiceKey = this.cloudFoundryClient.createServiceKey(cloudService, UUID.randomUUID().toString());
        logger.info("Finish creating service key for {} .", serviceName);
        return cloudServiceKey;
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
        CloudFoundryClient cloudFoundryClientUser;
        if (org != null && space != null) {
            cloudFoundryClientUser = cloudFoundryClientFactory.createCloudFoundryClient(token, this.cloudFoundryClient.getCloudControllerUrl(), org, space);
        } else {
            cloudFoundryClientUser = cloudFoundryClientFactory.createCloudFoundryClient(token, this.cloudFoundryClient.getCloudControllerUrl());
        }
        return cloudFoundryClientUser.getService(serviceName);
    }
}
