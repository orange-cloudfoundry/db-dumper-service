package com.orange.clara.cloud.servicedbdumper.model;

/**
 * Copyright (C) 2015 Orange
 * <p/>
 * This software is distributed under the terms and conditions of the 'Apache-2.0'
 * license which can be found in the file 'LICENSE' in this package distribution
 * or at 'https://opensource.org/licenses/Apache-2.0'.
 * <p/>
 * Author: Arthur Halet
 * Date: 10/12/2015
 */
public class MappedRequestInfo {

    private String name;
    private String url;

    private String description;

    public MappedRequestInfo(String name, String url) {
        this.name = name;
        this.url = url;
    }

    public MappedRequestInfo(String name, String url, String description) {
        this(name, url);
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    @Override
    public int hashCode() {
        return url.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MappedRequestInfo that = (MappedRequestInfo) o;

        return url.equals(that.url);

    }

    @Override
    public String toString() {
        return "MappedRequestInfo{" +
                "name='" + name + '\'' +
                ", url='" + url + '\'' +
                ", description='" + description + '\'' +
                '}';
    }
}
