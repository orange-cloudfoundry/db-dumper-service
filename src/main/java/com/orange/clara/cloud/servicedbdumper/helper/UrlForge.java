package com.orange.clara.cloud.servicedbdumper.helper;

import com.orange.clara.cloud.servicedbdumper.model.DatabaseDumpFile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.net.URI;

/**
 * Copyright (C) 2016 Orange
 * <p/>
 * This software is distributed under the terms and conditions of the 'Apache-2.0'
 * license which can be found in the file 'LICENSE' in this package distribution
 * or at 'https://opensource.org/licenses/Apache-2.0'.
 * <p/>
 * Author: Arthur Halet
 * Date: 14/02/2016
 */
@Service
public class UrlForge {
    @Autowired
    @Qualifier("appUri")
    private String appUri;

    public String createDownloadLink(DatabaseDumpFile databaseDumpFile) {
        URI appInUri = URI.create(appUri);
        String port = "";
        if (appInUri.getPort() != -1) {
            port = ":" + appInUri.getPort();
        }
        return appInUri.getScheme() + "://" +
                databaseDumpFile.getUser() + ":" +
                databaseDumpFile.getPassword() + "@" +
                appInUri.getHost() + port + "/manage/download/" +
                databaseDumpFile.getId();
    }
}
