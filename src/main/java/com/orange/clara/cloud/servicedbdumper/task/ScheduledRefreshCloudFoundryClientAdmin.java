package com.orange.clara.cloud.servicedbdumper.task;

import org.cloudfoundry.client.lib.CloudFoundryClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Copyright (C) 2016 Orange
 * <p>
 * This software is distributed under the terms and conditions of the 'Apache-2.0'
 * license which can be found in the file 'LICENSE' in this package distribution
 * or at 'https://opensource.org/licenses/Apache-2.0'.
 * <p>
 * Author: Arthur Halet
 * Date: 13/05/2016
 */
@Component
public class ScheduledRefreshCloudFoundryClientAdmin {

    @Autowired
    @Qualifier("cloudFoundryClientAsAdmin")
    private CloudFoundryClient cloudFoundryClient;

    @Scheduled(fixedDelay = 900)
    public void refreshCloudFoundryClientAdmin() {
        if (cloudFoundryClient == null) {
            return;
        }
        cloudFoundryClient.login();
    }
}
