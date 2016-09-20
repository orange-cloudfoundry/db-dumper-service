package com.orange.clara.cloud.servicedbdumper.model;

import com.google.common.collect.Lists;

import java.util.Date;
import java.util.List;

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
    private Long size;
    private Boolean deleted;
    private List<String> tags;
    private String databaseName;
    private DatabaseType databaseType;

    public DbDumperCredential() {
        this.tags = Lists.newArrayList();
    }

    public DbDumperCredential(Integer id, Date createdAt, String downloadUrl, String showUrl, String filename, long size, Boolean deleted, DatabaseType databaseType, String databaseName) {
        this();
        this.databaseType = databaseType;
        this.id = id;
        this.createdAt = createdAt;
        this.downloadUrl = downloadUrl;
        this.showUrl = showUrl;
        this.filename = filename;
        this.size = size;
        this.deleted = deleted;
        this.databaseName = databaseName;
    }

    public DbDumperCredential(Integer id, Date createdAt, String downloadUrl, String showUrl, String filename, long size, Boolean deleted, DatabaseType databaseType, String databaseName, List<String> tags) {
        this(id, createdAt, downloadUrl, showUrl, filename, size, deleted, databaseType, databaseName);
        this.tags = tags;
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

    public Long getSize() {
        return size;
    }

    public void setSize(Long size) {
        this.size = size;
    }

    public Boolean getDeleted() {
        return deleted;
    }

    public void setDeleted(Boolean deleted) {
        this.deleted = deleted;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public DatabaseType getDatabaseType() {
        return databaseType;
    }

    public void setDatabaseType(DatabaseType databaseType) {
        this.databaseType = databaseType;
    }

    public String getDatabaseName() {
        return databaseName;
    }

    public void setDatabaseName(String databaseName) {
        this.databaseName = databaseName;
    }

    public boolean hasOneOfTags(String... tags) {
        for (String tag : tags) {
            if (this.tags.contains(tag)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DbDumperCredential)) return false;

        DbDumperCredential that = (DbDumperCredential) o;

        return id != null ? id.equals(that.id) : that.id == null;

    }
}
