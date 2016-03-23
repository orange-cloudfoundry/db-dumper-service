package com.orange.clara.cloud.servicedbdumper.fake.cloudservice;

import org.cloudfoundry.client.lib.domain.CloudService;

import java.util.Date;
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
public class MockCloudService extends CloudService {


    public MockCloudService(String serviceName) {
        super();
        this.setName(serviceName);
        this.setLabel("fakelabel");
        this.setPlan("fakeplan");
        this.setProvider("fakeprovider");
        this.setVersion("0.0.1");
        this.setMeta(this.generateMeta());
    }

    private Meta generateMeta() {
        return new Meta(UUID.randomUUID(), new Date(), new Date());
    }
}
