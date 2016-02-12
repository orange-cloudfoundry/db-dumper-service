package com.orange.clara.cloud.servicedbdumper.config;

import com.orange.clara.cloud.servicedbdumper.security.CloudFoundryUserAccessRight;
import com.orange.clara.cloud.servicedbdumper.security.DefaultUserAccessRight;
import com.orange.clara.cloud.servicedbdumper.security.UserAccessRight;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 * Copyright (C) 2016 Arthur Halet
 * <p/>
 * This software is distributed under the terms and conditions of the 'MIT'
 * license which can be found in the file 'LICENSE' in this package distribution
 * or at 'http://opensource.org/licenses/MIT'.
 * <p/>
 * Author: Arthur Halet
 * Date: 12/02/2016
 */
@Configuration
public class AppConfig {

    @Bean(name = "userAccessRight")
    @Profile("uaa")
    public UserAccessRight getCloudFoundryUserAccessRight() {
        return new CloudFoundryUserAccessRight();
    }

    @Bean(name = "userAccessRight")
    @Profile("!uaa")
    public UserAccessRight getDefaultUserAccessRight() {
        return new DefaultUserAccessRight();
    }
}
