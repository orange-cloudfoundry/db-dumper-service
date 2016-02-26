package com.orange.clara.cloud.servicedbdumper.config;


import com.orange.clara.cloud.servicedbdumper.filer.s3uploader.UploadS3Stream;
import com.orange.clara.cloud.servicedbdumper.filer.s3uploader.UploadS3StreamImpl;
import com.orange.spring.cloud.connector.s3.core.jcloudswrappers.SpringCloudBlobStore;
import com.orange.spring.cloud.connector.s3.core.jcloudswrappers.SpringCloudBlobStoreContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.config.java.AbstractCloudConfig;
import org.springframework.cloud.config.java.ServiceScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 * Copyright (C) 2015 Orange
 * <p>
 * This software is distributed under the terms and conditions of the 'Apache-2.0'
 * license which can be found in the file 'LICENSE' in this package distribution
 * or at 'https://opensource.org/licenses/Apache-2.0'.
 * <p>
 * Author: Arthur Halet
 * Date: 03/06/2015
 */
@Configuration
@Profile({"core", "s3"})
@ServiceScan
public class CloudConfig extends AbstractCloudConfig {
    @Autowired
    private SpringCloudBlobStoreContext springCloudBlobStoreContext;

    @Bean
    public SpringCloudBlobStoreContext blobStoreContext() {
        return this.springCloudBlobStoreContext;
    }

    @Bean
    public SpringCloudBlobStore blobStore() {
        return this.blobStoreContext().getSpringCloudBlobStore();
    }

    @Bean
    public UploadS3Stream uploadS3Stream() {
        return new UploadS3StreamImpl();
    }
}
