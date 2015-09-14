package com.orange.clara.cloud.riak;

import org.springframework.cloud.service.UriBasedServiceInfo;


public class RiakcsServiceInfo extends UriBasedServiceInfo {
    public RiakcsServiceInfo(String id, String scheme, String host, int port, String username, String password, String path) {
        super(id, scheme, host, port, username, password, path);
    }

    public RiakcsServiceInfo(String id, String uriString) {
        super(id, uriString);
    }


    public String getBucket() {
        return this.getPath().substring(1);
    }
}
