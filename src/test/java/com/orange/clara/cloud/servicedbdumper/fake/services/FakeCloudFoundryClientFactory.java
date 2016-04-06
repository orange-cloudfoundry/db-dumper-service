package com.orange.clara.cloud.servicedbdumper.fake.services;

import com.orange.clara.cloud.servicedbdumper.cloudfoundry.CloudFoundryClientFactory;
import org.cloudfoundry.client.lib.CloudFoundryClient;
import org.springframework.security.oauth2.common.OAuth2AccessToken;

import java.net.MalformedURLException;
import java.net.URL;

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
public class FakeCloudFoundryClientFactory extends CloudFoundryClientFactory {
    private CloudFoundryClient cloudFoundryClient;

    public FakeCloudFoundryClientFactory(CloudFoundryClient cloudFoundryClient) {
        this.cloudFoundryClient = cloudFoundryClient;
    }

    @Override
    public CloudFoundryClient createCloudFoundryClient(OAuth2AccessToken token, String cloudControllerUrl) throws MalformedURLException {
        return this.cloudFoundryClient;
    }

    @Override
    public CloudFoundryClient createCloudFoundryClient(OAuth2AccessToken token, URL cloudControllerUrl) {
        return this.cloudFoundryClient;
    }

    @Override
    public CloudFoundryClient createCloudFoundryClient(OAuth2AccessToken token, URL cloudControllerUrl, String org, String space) {
        return this.cloudFoundryClient;
    }

    @Override
    public CloudFoundryClient createCloudFoundryClient(OAuth2AccessToken token, String cloudControllerUrl, String org, String space) throws MalformedURLException {
        return this.cloudFoundryClient;
    }

    @Override
    public CloudFoundryClient createCloudFoundryClient(String token, String cloudControllerUrl) throws MalformedURLException {
        return this.cloudFoundryClient;
    }

    @Override
    public CloudFoundryClient createCloudFoundryClient(String token, URL cloudControllerUrl) {
        return this.cloudFoundryClient;
    }

    @Override
    public CloudFoundryClient createCloudFoundryClient(String token, URL cloudControllerUrl, String org, String space) {
        return this.cloudFoundryClient;
    }

    @Override
    public CloudFoundryClient createCloudFoundryClient(String token, String cloudControllerUrl, String org, String space) throws MalformedURLException {
        return this.cloudFoundryClient;
    }

    @Override
    public CloudFoundryClient createCloudFoundryClient(String user, String password, String cloudControllerUrl) throws MalformedURLException {
        return this.cloudFoundryClient;
    }

    @Override
    public CloudFoundryClient createCloudFoundryClient(String user, String password, URL cloudControllerUrl) {
        return this.cloudFoundryClient;
    }

    @Override
    public CloudFoundryClient createCloudFoundryClient(String user, String password, String cloudControllerUrl, String org, String space) throws MalformedURLException {
        return this.cloudFoundryClient;
    }

    @Override
    public CloudFoundryClient createCloudFoundryClient(String user, String password, URL cloudControllerUrl, String org, String space) {
        return this.cloudFoundryClient;
    }

    public CloudFoundryClient getCloudFoundryClient() {
        return cloudFoundryClient;
    }

    public void setCloudFoundryClient(CloudFoundryClient cloudFoundryClient) {
        this.cloudFoundryClient = cloudFoundryClient;
    }
}
