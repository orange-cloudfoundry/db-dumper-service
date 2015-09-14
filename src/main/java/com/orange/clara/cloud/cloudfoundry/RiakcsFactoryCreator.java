package com.orange.clara.cloud.cloudfoundry;

import com.google.common.collect.ImmutableSet;
import com.google.inject.Module;
import com.orange.clara.cloud.riak.RiakcsServiceInfo;
import org.jclouds.ContextBuilder;
import org.jclouds.blobstore.BlobStoreContext;
import org.jclouds.logging.slf4j.config.SLF4JLoggingModule;
import org.springframework.cloud.service.AbstractServiceConnectorCreator;
import org.springframework.cloud.service.ServiceConnectorConfig;

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
 * Date: 04/06/2015
 */
public class RiakcsFactoryCreator extends AbstractServiceConnectorCreator<RiakcsContextBuilder, RiakcsServiceInfo> {
    @Override
    public RiakcsContextBuilder create(RiakcsServiceInfo serviceInfo, ServiceConnectorConfig serviceConnectorConfig) {
        String key = serviceInfo.getUserName();
        String secret = serviceInfo.getPassword();

        String port = "";
        if (serviceInfo.getPort() != -1) {
            port += ":" + serviceInfo.getPort();
        }
        String host = serviceInfo.getHost() + port;
        Properties storeProviderInitProperties = new Properties();
        storeProviderInitProperties.put(PROPERTY_TRUST_ALL_CERTS, true);
        storeProviderInitProperties.put(PROPERTY_RELAX_HOSTNAME, true);
        storeProviderInitProperties.put(PROPERTY_S3_VIRTUAL_HOST_BUCKETS, false);
        RiakcsContextBuilder riakcsContextBuilder = new RiakcsContextBuilder();
        riakcsContextBuilder.getContextBuilder().overrides(storeProviderInitProperties).endpoint(host).credentials(key, secret);
        riakcsContextBuilder.setBucketName(serviceInfo.getBucket());
        return riakcsContextBuilder;
    }
}
