package com.orange.clara.cloud.servicedbdumper.dbdumper.running.core;

import com.google.common.collect.Maps;
import com.orange.clara.cloud.servicedbdumper.dbdumper.running.Credentials;
import com.orange.clara.cloud.servicedbdumper.model.DbDumperServiceInstance;
import org.jclouds.blobstore.BlobStoreContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;

import java.util.Map;

/**
 * Copyright (C) 2015 Orange
 * <p/>
 * This software is distributed under the terms and conditions of the 'MIT'
 * license which can be found in the file 'LICENSE' in this package distribution
 * or at 'http://opensource.org/licenses/MIT'.
 * <p/>
 * Author: Arthur Halet
 * Date: 26/11/2015
 */
public class CoreCredentials implements Credentials {

    private static final String LIST_URL = "/manage/list/%s";
    @Autowired
    @Qualifier(value = "bucketName")
    protected String bucketName;
    @Autowired
    @Qualifier(value = "blobStoreContext")
    protected BlobStoreContext blobStoreContext;
    @Value("${vcap.application.uris[0]:localhost:8080}")
    private String appUri;

    @Override
    public Map<String, String> getCredentials(DbDumperServiceInstance dbDumperServiceInstance) {
        Map<String, String> credentials = Maps.newHashMap();
        credentials.put("bucket_folder", dbDumperServiceInstance.getDatabaseRef().getDatabaseName());
        credentials.put("list_dump", String.format(this.appUri + LIST_URL, dbDumperServiceInstance.getServiceInstanceId()));
        return credentials;
    }
}
