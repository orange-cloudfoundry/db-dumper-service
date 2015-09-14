package com.orange.clara.cloud.config;

import com.basho.riakcs.client.api.RiakCSClient;
import com.orange.clara.cloud.cloudfoundry.RiakcsContextBuilder;
import com.orange.clara.cloud.cloudfoundry.RiakcsFactoryCreator;
import com.orange.clara.cloud.riak.RiakcsClientCloudFoundry;
import com.orange.clara.cloud.riak.RiakcsServiceInfo;
import org.jclouds.blobstore.BlobStoreContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.util.Properties;

import static org.jclouds.Constants.PROPERTY_RELAX_HOSTNAME;
import static org.jclouds.Constants.PROPERTY_TRUST_ALL_CERTS;
import static org.jclouds.s3.reference.S3Constants.PROPERTY_S3_VIRTUAL_HOST_BUCKETS;

/**
 * Copyright (C) 2015 Arthur Halet
 * <p/>
 * This software is distributed under the terms and conditions of the 'MIT'
 * license which can be found in the file 'LICENSE' in this package distribution
 * or at 'http://opensource.org/licenses/MIT'.
 * <p/>
 * Author: Arthur Halet
 * Date: 03/06/2015
 */
@Configuration
@Profile("local")
public class LocalConfig {
    @Bean
    public BlobStoreContext blobStoreContext() {
        RiakcsServiceInfo riakcsServiceInfo = new RiakcsServiceInfo("local", "s3", "localhost", 80, "access", "secret", "mybucket");
        Properties storeProviderInitProperties = new Properties();
        storeProviderInitProperties.put(PROPERTY_TRUST_ALL_CERTS, true);
        storeProviderInitProperties.put(PROPERTY_RELAX_HOSTNAME, true);
        storeProviderInitProperties.put(PROPERTY_S3_VIRTUAL_HOST_BUCKETS, false);
        RiakcsFactoryCreator riakcsFactoryCreator = new RiakcsFactoryCreator();
        RiakcsContextBuilder riakcsContextBuilder = riakcsFactoryCreator.create(riakcsServiceInfo, null);
        return riakcsContextBuilder.getContextBuilder().overrides(storeProviderInitProperties).buildView(BlobStoreContext.class);
    }
}
