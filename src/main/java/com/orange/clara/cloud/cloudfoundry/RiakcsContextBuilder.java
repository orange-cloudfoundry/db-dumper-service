package com.orange.clara.cloud.cloudfoundry;

import org.jclouds.ContextBuilder;
import org.jclouds.apis.ApiMetadata;
import org.jclouds.providers.ProviderMetadata;

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
public class RiakcsContextBuilder {
    private String bucketName;
    private ContextBuilder contextBuilder;

    public ContextBuilder getContextBuilder() {
        if (this.contextBuilder == null) {
            this.contextBuilder = ContextBuilder.newBuilder("s3");
        }
        return contextBuilder;
    }


    public String getBucketName() {
        return bucketName;
    }

    public RiakcsContextBuilder setBucketName(String bucketName) {
        this.bucketName = bucketName;
        return this;
    }
}
