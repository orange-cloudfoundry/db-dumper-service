package com.orange.clara.cloud.servicedbdumper.cloudfoundry;

import org.cloudfoundry.client.lib.CloudCredentials;
import org.cloudfoundry.client.lib.CloudFoundryClient;
import org.springframework.security.oauth2.common.DefaultOAuth2AccessToken;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.stereotype.Service;

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
 * Date: 17/03/2016
 */
@Service
public class CloudFoundryClientFactory {

    public CloudFoundryClient createCloudFoundryClient(OAuth2AccessToken token, String cloudControllerUrl) throws MalformedURLException {
        return this.createCloudFoundryClient(token, new URL(cloudControllerUrl));
    }

    public CloudFoundryClient createCloudFoundryClient(OAuth2AccessToken token, URL cloudControllerUrl) {
        CloudCredentials credentials = new CloudCredentials(token, false);
        return new CloudFoundryClient(credentials, cloudControllerUrl);
    }

    public CloudFoundryClient createCloudFoundryClient(OAuth2AccessToken token, URL cloudControllerUrl, String org, String space) {
        CloudCredentials credentials = new CloudCredentials(token, false);
        return new CloudFoundryClient(credentials, cloudControllerUrl, org, space);
    }

    public CloudFoundryClient createCloudFoundryClient(OAuth2AccessToken token, String cloudControllerUrl, String org, String space) throws MalformedURLException {
        return this.createCloudFoundryClient(token, new URL(cloudControllerUrl), org, space);
    }

    public CloudFoundryClient createCloudFoundryClient(String token, String cloudControllerUrl) throws MalformedURLException {
        return this.createCloudFoundryClient(token, new URL(cloudControllerUrl));
    }

    public CloudFoundryClient createCloudFoundryClient(String token, URL cloudControllerUrl) {
        CloudCredentials credentials = new CloudCredentials(new DefaultOAuth2AccessToken(token), false);
        return new CloudFoundryClient(credentials, cloudControllerUrl);
    }

    public CloudFoundryClient createCloudFoundryClient(String token, URL cloudControllerUrl, String org, String space) {
        CloudCredentials credentials = new CloudCredentials(new DefaultOAuth2AccessToken(token), false);
        return new CloudFoundryClient(credentials, cloudControllerUrl, org, space);
    }

    public CloudFoundryClient createCloudFoundryClient(String token, String cloudControllerUrl, String org, String space) throws MalformedURLException {
        return this.createCloudFoundryClient(token, new URL(cloudControllerUrl), org, space);
    }

    public CloudFoundryClient createCloudFoundryClient(String user, String password, String cloudControllerUrl) throws MalformedURLException {
        CloudCredentials credentials = new CloudCredentials(user, password);
        return new CloudFoundryClient(credentials, new URL(cloudControllerUrl));
    }


    public CloudFoundryClient createCloudFoundryClient(String user, String password, URL cloudControllerUrl) {
        CloudCredentials credentials = new CloudCredentials(user, password);
        return new CloudFoundryClient(credentials, cloudControllerUrl);
    }

    public CloudFoundryClient createCloudFoundryClient(String user, String password, String cloudControllerUrl, String org, String space) throws MalformedURLException {
        CloudCredentials credentials = new CloudCredentials(user, password);
        return new CloudFoundryClient(credentials, new URL(cloudControllerUrl), org, space);
    }

    public CloudFoundryClient createCloudFoundryClient(String user, String password, URL cloudControllerUrl, String org, String space) {
        CloudCredentials credentials = new CloudCredentials(user, password);
        return new CloudFoundryClient(credentials, cloudControllerUrl, org, space);
    }


}
