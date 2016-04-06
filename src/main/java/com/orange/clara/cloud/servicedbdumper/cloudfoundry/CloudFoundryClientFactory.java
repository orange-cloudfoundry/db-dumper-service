package com.orange.clara.cloud.servicedbdumper.cloudfoundry;

import org.cloudfoundry.client.lib.CloudCredentials;
import org.cloudfoundry.client.lib.CloudFoundryClient;
import org.cloudfoundry.client.lib.HttpProxyConfiguration;
import org.springframework.security.oauth2.common.DefaultOAuth2AccessToken;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
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

    private static final boolean SKIP_SSL_VERIFICATION = Boolean.getBoolean("skip.ssl.verification");

    private static final String CCNG_API_PROXY_HOST = System.getProperty("http.proxyHost", null);

    private static final String CCNG_API_PROXY_PASSWD = System.getProperty("http.proxyPassword", null);

    private static final int CCNG_API_PROXY_PORT = Integer.getInteger("http.proxyPort", 80);

    private static final String CCNG_API_PROXY_USER = System.getProperty("http.proxyUsername", null);

    private static HttpProxyConfiguration httpProxyConfiguration;

    @PostConstruct
    public static void createProxyIfExists() throws Exception {
        if (CCNG_API_PROXY_HOST == null) {
            return;
        }
        if (CCNG_API_PROXY_USER != null) {
            httpProxyConfiguration = new HttpProxyConfiguration(CCNG_API_PROXY_HOST, CCNG_API_PROXY_PORT, true,
                    CCNG_API_PROXY_USER, CCNG_API_PROXY_PASSWD);
        }
        httpProxyConfiguration = new HttpProxyConfiguration(CCNG_API_PROXY_HOST, CCNG_API_PROXY_PORT);
    }

    public CloudFoundryClient createCloudFoundryClient(OAuth2AccessToken token, String cloudControllerUrl) throws MalformedURLException {
        return this.createCloudFoundryClient(token, new URL(cloudControllerUrl));
    }

    public CloudFoundryClient createCloudFoundryClient(OAuth2AccessToken token, URL cloudControllerUrl) {
        CloudCredentials credentials = new CloudCredentials(token, false);
        return new CloudFoundryClient(credentials, cloudControllerUrl, httpProxyConfiguration, SKIP_SSL_VERIFICATION);
    }

    public CloudFoundryClient createCloudFoundryClient(OAuth2AccessToken token, URL cloudControllerUrl, String org, String space) {
        CloudCredentials credentials = new CloudCredentials(token, false);
        if (httpProxyConfiguration == null) {
            return new CloudFoundryClient(credentials, cloudControllerUrl, org, space);
        }
        return new CloudFoundryClient(credentials, cloudControllerUrl, org, space, httpProxyConfiguration);
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
        return new CloudFoundryClient(credentials, cloudControllerUrl, org, space, httpProxyConfiguration, SKIP_SSL_VERIFICATION);
    }

    public CloudFoundryClient createCloudFoundryClient(String token, String cloudControllerUrl, String org, String space) throws MalformedURLException {
        return this.createCloudFoundryClient(token, new URL(cloudControllerUrl), org, space);
    }

    public CloudFoundryClient createCloudFoundryClient(String user, String password, String cloudControllerUrl) throws MalformedURLException {
        CloudCredentials credentials = new CloudCredentials(user, password);

        return new CloudFoundryClient(credentials, new URL(cloudControllerUrl), httpProxyConfiguration, SKIP_SSL_VERIFICATION);


    }

    public CloudFoundryClient createCloudFoundryClient(String user, String password, URL cloudControllerUrl) {
        CloudCredentials credentials = new CloudCredentials(user, password);
        return new CloudFoundryClient(credentials, cloudControllerUrl, httpProxyConfiguration, SKIP_SSL_VERIFICATION);
    }

    public CloudFoundryClient createCloudFoundryClient(String user, String password, String cloudControllerUrl, String org, String space) throws MalformedURLException {
        CloudCredentials credentials = new CloudCredentials(user, password);
        return new CloudFoundryClient(credentials, new URL(cloudControllerUrl), org, space, httpProxyConfiguration, SKIP_SSL_VERIFICATION);
    }

    public CloudFoundryClient createCloudFoundryClient(String user, String password, URL cloudControllerUrl, String org, String space) {
        CloudCredentials credentials = new CloudCredentials(user, password);
        return new CloudFoundryClient(credentials, cloudControllerUrl, org, space, httpProxyConfiguration, SKIP_SSL_VERIFICATION);
    }

}
