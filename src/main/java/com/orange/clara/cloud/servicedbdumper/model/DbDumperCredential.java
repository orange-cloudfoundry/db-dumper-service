package com.orange.clara.cloud.servicedbdumper.model;

import java.util.Date;

/**
 * Copyright (C) 2016 Orange
 * <p>
 * This software is distributed under the terms and conditions of the 'Apache-2.0'
 * license which can be found in the file 'LICENSE' in this package distribution
 * or at 'https://opensource.org/licenses/Apache-2.0'.
 * <p>
 * Author: Arthur Halet
 * Date: 20/02/2016
 */
public class DbDumperCredential {
    private Integer id;
    private Date createdAt;
    private String downloadUrl;
    private String showUrl;
    private String filename;

    public DbDumperCredential() {
    }

    public DbDumperCredential(Integer id, Date createdAt, String downloadUrl, String showUrl, String filename) {
        this.id = id;
        this.createdAt = createdAt;
        this.downloadUrl = downloadUrl;
        this.showUrl = showUrl;
        this.filename = filename;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public String getDownloadUrl() {
        return downloadUrl;
    }

    public void setDownloadUrl(String downloadUrl) {
        this.downloadUrl = downloadUrl;
    }

    public String getShowUrl() {
        return showUrl;
    }

    public void setShowUrl(String showUrl) {
        this.showUrl = showUrl;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }
}
