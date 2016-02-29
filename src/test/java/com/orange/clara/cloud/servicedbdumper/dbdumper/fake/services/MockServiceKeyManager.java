package com.orange.clara.cloud.servicedbdumper.dbdumper.fake.services;

import com.orange.clara.cloud.servicedbdumper.exception.ServiceKeyException;
import com.orange.clara.cloud.servicedbdumper.model.DatabaseService;
import com.orange.clara.cloud.servicedbdumper.service.servicekey.ServiceKeyManager;
import org.cloudfoundry.client.lib.domain.CloudServiceKey;

/**
 * Copyright (C) 2016 Orange
 * <p>
 * This software is distributed under the terms and conditions of the 'Apache-2.0'
 * license which can be found in the file 'LICENSE' in this package distribution
 * or at 'https://opensource.org/licenses/Apache-2.0'.
 * <p>
 * Author: Arthur Halet
 * Date: 29/02/2016
 */
public class MockServiceKeyManager implements ServiceKeyManager {

    private CloudServiceKey cloudServiceKey;

    public MockServiceKeyManager(CloudServiceKey cloudServiceKey) {
        this.cloudServiceKey = cloudServiceKey;
    }

    public MockServiceKeyManager() {

    }

    @Override
    public CloudServiceKey createServiceKey(String serviceName, String token, String org, String space) throws ServiceKeyException {
        return cloudServiceKey;
    }

    @Override
    public CloudServiceKey createServiceKey(DatabaseService databaseService) throws ServiceKeyException {
        return cloudServiceKey;
    }

    @Override
    public void deleteServiceKey(String guid) {

    }

    @Override
    public void deleteServiceKey(DatabaseService databaseService) {

    }

    public CloudServiceKey getCloudServiceKey() {
        return cloudServiceKey;
    }

    public void setCloudServiceKey(CloudServiceKey cloudServiceKey) {
        this.cloudServiceKey = cloudServiceKey;
    }
}
