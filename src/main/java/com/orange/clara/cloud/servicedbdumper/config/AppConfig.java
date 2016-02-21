package com.orange.clara.cloud.servicedbdumper.config;

import com.orange.clara.cloud.servicedbdumper.security.CloudFoundryUserAccessRight;
import com.orange.clara.cloud.servicedbdumper.security.DefaultUserAccessRight;
import com.orange.clara.cloud.servicedbdumper.security.UserAccessRight;
import com.orange.clara.cloud.servicedbdumper.service.servicekey.CloudFoundryServiceKeyManager;
import com.orange.clara.cloud.servicedbdumper.service.servicekey.NullServiceKeyManager;
import com.orange.clara.cloud.servicedbdumper.service.servicekey.ServiceKeyManager;
import org.cloudfoundry.client.lib.CloudCredentials;
import org.cloudfoundry.client.lib.CloudFoundryClient;
import org.cloudfoundry.client.lib.domain.CloudOrganization;
import org.cloudfoundry.client.lib.domain.CloudSpace;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.net.MalformedURLException;
import java.net.URL;
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

    private Logger logger = LoggerFactory.getLogger(AppConfig.class);
    @Value("${file.date.format:dd-MM-yyyy HH:mm}")
    private String dateFormat;


    @Value("${cf.admin.user:#{null}}")
    private String cfAdminUser;
    @Value("${cf.admin.password:#{null}}")
    private String cfAdminPassword;
    @Value("${cloud.controller.url:#{null}}")
    private String cloudControllerUrl;

    @Bean
    public String dateFormatFile() {
        String finalDateFormat = dateFormat.replaceAll(Pattern.quote(" "), "-");
        finalDateFormat = finalDateFormat.replaceAll(Pattern.quote(":"), "");
        return finalDateFormat;
    }

    @Bean
    public ServiceKeyManager serviceKeyManager() {
        if (this.cfAdminUser == null
                || this.cfAdminUser.isEmpty()
                || this.cfAdminPassword == null
                || this.cfAdminPassword.isEmpty()
                || this.cloudControllerUrl == null
                || this.cloudControllerUrl.isEmpty()) {
            return new NullServiceKeyManager();
        }
        return new CloudFoundryServiceKeyManager();
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

    @Bean(name = "cloudFoundryClientAsAdmin")
    public CloudFoundryClient getCloudFoundryClientAsAdmin() throws MalformedURLException {
        if (this.cfAdminUser == null
                || this.cfAdminUser.isEmpty()
                || this.cfAdminPassword == null
                || this.cfAdminPassword.isEmpty()
                || this.cloudControllerUrl == null
                || this.cloudControllerUrl.isEmpty()) {
            return null;
        }
        CloudOrganization cloudOrganization = this.getOrg();
        CloudSpace cloudSpace = this.getSpace();
        logger.debug(String.format("Creating new CloudFoundry client using admin access with org '%s' and space '%s'", cloudOrganization.getName(), cloudSpace.getName()));
        CloudCredentials credentials = new CloudCredentials(this.cfAdminUser, this.cfAdminPassword);
        return new CloudFoundryClient(credentials, new URL(this.cloudControllerUrl), cloudOrganization.getName(), cloudSpace.getName());
    }

    @Bean
    public CloudFoundryClient getCloudFoundryClientTemp() throws MalformedURLException {
        if (this.cfAdminUser == null
                || this.cfAdminUser.isEmpty()
                || this.cfAdminPassword == null
                || this.cfAdminPassword.isEmpty()
                || this.cloudControllerUrl == null
                || this.cloudControllerUrl.isEmpty()) {
            return null;
        }
        logger.debug("Creating new CloudFoundry client using admin access");
        CloudCredentials credentials = new CloudCredentials(this.cfAdminUser, this.cfAdminPassword);
        return new CloudFoundryClient(credentials, new URL(this.cloudControllerUrl));
    }

    @Bean
    public CloudOrganization getOrg() throws MalformedURLException {
        CloudFoundryClient cloudFoundryClient = this.getCloudFoundryClientTemp();
        if (cloudFoundryClient == null) {
            return null;
        }
        return cloudFoundryClient.getOrganizations().get(0);
    }

    @Bean
    public CloudSpace getSpace() throws MalformedURLException {
        CloudFoundryClient cloudFoundryClient = this.getCloudFoundryClientTemp();
        if (cloudFoundryClient == null) {
            return null;
        }
        for (CloudSpace cloudSpace : getCloudFoundryClientTemp().getSpaces()) {
            if (cloudSpace.getOrganization().getMeta().getGuid().equals(this.getOrg().getMeta().getGuid())) {
                return cloudSpace;
            }
        }
        return null;
    }

}
