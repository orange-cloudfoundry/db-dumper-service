package com.orange.clara.cloud.servicedbdumper.dbdumper.fake.cloudservicekey;

import com.orange.clara.cloud.servicedbdumper.dbdumper.fake.cloudservice.MockCloudService;
import org.cloudfoundry.client.lib.domain.CloudServiceKey;

import java.util.Date;
import java.util.Map;
import java.util.UUID;

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
public class MockCloudServiceKey extends CloudServiceKey {
    public MockCloudServiceKey(Map<String, Object> credentials, String serviceName) {
        super();
        this.setCredentials(credentials);
        this.setMeta(this.generateMeta());
        this.setName("fakeservicekey");
        this.setService(new MockCloudService(serviceName));
    }

    private Meta generateMeta() {
        return new Meta(UUID.randomUUID(), new Date(), new Date());
    }
}
