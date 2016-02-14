package com.orange.clara.cloud.servicedbdumper.dbdumper.running.core;

import com.google.common.collect.Maps;
import com.orange.clara.cloud.servicedbdumper.dbdumper.running.Credentials;
import com.orange.clara.cloud.servicedbdumper.helper.UrlForge;
import com.orange.clara.cloud.servicedbdumper.model.DatabaseDumpFile;
import com.orange.clara.cloud.servicedbdumper.model.DbDumperServiceInstance;
import org.jclouds.blobstore.BlobStoreContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Copyright (C) 2015 Orange
 * <p>
 * This software is distributed under the terms and conditions of the 'Apache-2.0'
 * license which can be found in the file 'LICENSE' in this package distribution
 * or at 'https://opensource.org/licenses/Apache-2.0'.
 * <p>
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
    @Autowired
    @Qualifier("appUri")
    private String appUri;

    @Autowired
    @Qualifier(value = "dateFormat")
    private String dateFormat;

    @Autowired
    private UrlForge urlForge;

    @Override
    public Map<String, Object> getCredentials(DbDumperServiceInstance dbDumperServiceInstance) {
        SimpleDateFormat dateFormater = new SimpleDateFormat(this.dateFormat);
        Map<String, Object> credentials = Maps.newHashMap();
        List<Map<String, String>> dumpFiles = new ArrayList<>();
        Map<String, String> dumpFile;
        for (DatabaseDumpFile databaseDumpFile : dbDumperServiceInstance.getDatabaseRef().getDatabaseDumpFiles()) {
            dumpFile = Maps.newHashMap();
            dumpFile.put("download_url", urlForge.createDownloadLink(databaseDumpFile));
            dumpFile.put("show_url", urlForge.createShowLink(databaseDumpFile));
            dumpFile.put("created_at", dateFormater.format(databaseDumpFile.getCreatedAt()));
            dumpFiles.add(dumpFile);
        }
        credentials.put("dumps", dumpFiles);
        return credentials;
    }
}
