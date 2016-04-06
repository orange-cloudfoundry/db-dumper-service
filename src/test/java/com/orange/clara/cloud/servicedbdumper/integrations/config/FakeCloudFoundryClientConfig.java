package com.orange.clara.cloud.servicedbdumper.integrations.config;

import com.orange.clara.cloud.servicedbdumper.cloudfoundry.CloudFoundryClientFactory;
import com.orange.clara.cloud.servicedbdumper.fake.cloudfoundry.CloudFoundryClientFake;
import com.orange.clara.cloud.servicedbdumper.fake.services.FakeCloudFoundryClientFactory;
import com.orange.clara.cloud.servicedbdumper.service.servicekey.CloudFoundryServiceKeyManager;
import com.orange.clara.cloud.servicedbdumper.service.servicekey.ServiceKeyManager;
import org.cloudfoundry.client.lib.CloudFoundryClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

/**
 * Copyright (C) 2016 Orange
 * <p>
 * This software is distributed under the terms and conditions of the 'Apache-2.0'
 * license which can be found in the file 'LICENSE' in this package distribution
 * or at 'https://opensource.org/licenses/Apache-2.0'.
 * <p>
 * Author: Arthur Halet
 * Date: 06/04/2016
 */
@Configuration
@Profile("integration-fake-cf-client")
public class FakeCloudFoundryClientConfig {
    private Logger logger = LoggerFactory.getLogger(FakeCloudFoundryClientConfig.class);

    @Bean
    @Primary
    public ServiceKeyManager serviceKeyManager() {
        return new CloudFoundryServiceKeyManager();
    }

    @Bean(name = "cloudFoundryClientAsAdmin")
    @Primary
    public CloudFoundryClient getCloudFoundryClientAsAdmin() {
        logger.debug("Creating fake cloud foundry client admin...");
        return new CloudFoundryClientFake();
    }

    @Bean
    @Primary
    public CloudFoundryClientFactory cloudFoundryClientFactory() {
        return new FakeCloudFoundryClientFactory(this.getCloudFoundryClientAsAdmin());
    }
}
