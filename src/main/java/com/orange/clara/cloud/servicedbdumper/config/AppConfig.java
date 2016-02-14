package com.orange.clara.cloud.servicedbdumper.config;

import com.orange.clara.cloud.servicedbdumper.security.CloudFoundryUserAccessRight;
import com.orange.clara.cloud.servicedbdumper.security.DefaultUserAccessRight;
import com.orange.clara.cloud.servicedbdumper.security.UserAccessRight;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.util.regex.Pattern;

/**
 * Copyright (C) 2016 Orange
 * <p>
 * This software is distributed under the terms and conditions of the 'Apache-2.0'
 * license which can be found in the file 'LICENSE' in this package distribution
 * or at 'https://opensource.org/licenses/Apache-2.0'.
 * <p>
 * Author: Arthur Halet
 * Date: 12/02/2016
 */
@Configuration
public class AppConfig {
    @Value("${file.date.format:dd-MM-yyyy HH:mm}")
    private String dateFormat;


    @Bean
    public String dateFormatFile() {
        String finalDateFormat = dateFormat.replaceAll(Pattern.quote(" "), "-");
        finalDateFormat = finalDateFormat.replaceAll(Pattern.quote(":"), "");
        return finalDateFormat;
    }

    @Bean
    public String dateFormat() {
        return this.dateFormat;
    }

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
