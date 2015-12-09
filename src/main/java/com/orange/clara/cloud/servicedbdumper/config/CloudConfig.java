package com.orange.clara.cloud.servicedbdumper.config;


import com.orange.cloudfoundry.connector.s3.factory.S3ContextBuilder;
import org.jclouds.blobstore.BlobStoreContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.config.java.AbstractCloudConfig;
import org.springframework.cloud.config.java.ServiceScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.util.Properties;

import static org.jclouds.Constants.PROPERTY_RELAX_HOSTNAME;
import static org.jclouds.Constants.PROPERTY_TRUST_ALL_CERTS;

/**
 * Copyright (C) 2015 Orange
 * <p/>
 * This software is distributed under the terms and conditions of the 'MIT'
 * license which can be found in the file 'LICENSE' in this package distribution
 * or at 'http://opensource.org/licenses/MIT'.
 * <p/>
 * Author: Arthur Halet
 * Date: 03/06/2015
 */
@Configuration
@Profile({"cloud"})
@ServiceScan
public class CloudConfig extends AbstractCloudConfig {

    @Profile({"core"})
    @Configuration
    @ServiceScan
    public static class CoreCloudConfig extends AbstractCloudConfig {
        @Autowired
        private S3ContextBuilder s3ContextBuilder;

        @Bean
        public String bucketName() {
            return this.s3ContextBuilder.getBucketName();
        }

        @Bean
        public BlobStoreContext blobStoreContext() {
            Properties storeProviderInitProperties = new Properties();
            storeProviderInitProperties.put(PROPERTY_TRUST_ALL_CERTS, true);
            storeProviderInitProperties.put(PROPERTY_RELAX_HOSTNAME, true);
            return this.s3ContextBuilder.getContextBuilder()
                    .overrides(storeProviderInitProperties)
                    .buildView(BlobStoreContext.class);
        }
    }

}
