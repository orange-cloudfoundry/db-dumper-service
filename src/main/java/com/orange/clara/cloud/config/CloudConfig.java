package com.orange.clara.cloud.config;

import com.orange.clara.cloud.cloudfoundry.RiakcsContextBuilder;

import org.jclouds.blobstore.BlobStoreContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.config.java.AbstractCloudConfig;
import org.springframework.cloud.config.java.ServiceScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.util.List;
import java.util.Properties;

import static org.jclouds.Constants.PROPERTY_RELAX_HOSTNAME;
import static org.jclouds.Constants.PROPERTY_TRUST_ALL_CERTS;
import static org.jclouds.s3.reference.S3Constants.PROPERTY_S3_VIRTUAL_HOST_BUCKETS;

@Configuration
@Profile(value = "cloud")
@ServiceScan
public class CloudConfig extends AbstractCloudConfig {
    @Autowired
    private RiakcsContextBuilder riakcsContextBuilder;

    public CloudConfig() {

    }

    @Bean
    public String bucketName() {
        return this.riakcsContextBuilder.getBucketName();
    }

    @Bean
    public BlobStoreContext blobStoreContext() {
        Properties storeProviderInitProperties = new Properties();
        storeProviderInitProperties.put(PROPERTY_TRUST_ALL_CERTS, true);
        storeProviderInitProperties.put(PROPERTY_RELAX_HOSTNAME, true);
        storeProviderInitProperties.put(PROPERTY_S3_VIRTUAL_HOST_BUCKETS, false);
        return this.riakcsContextBuilder.getContextBuilder().overrides(storeProviderInitProperties).buildView(BlobStoreContext.class);
    }
}
