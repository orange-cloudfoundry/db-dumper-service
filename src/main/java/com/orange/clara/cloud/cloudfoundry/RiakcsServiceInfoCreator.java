package com.orange.clara.cloud.cloudfoundry;

import com.orange.clara.cloud.riak.RiakcsServiceInfo;
import org.springframework.cloud.cloudfoundry.CloudFoundryServiceInfoCreator;
import org.springframework.cloud.cloudfoundry.Tags;

import java.util.Map;

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
public class RiakcsServiceInfoCreator extends CloudFoundryServiceInfoCreator<RiakcsServiceInfo> {
    public RiakcsServiceInfoCreator() {
        super(new Tags("riak-cs", "s3"));
    }

    @Override
    public RiakcsServiceInfo createServiceInfo(Map<String, Object> serviceData) {
        Map credentials = this.getCredentials(serviceData);
        String id = (String) serviceData.get("name");
        String uri = this.getUriFromCredentials(credentials);
        return new RiakcsServiceInfo(id, uri);
    }
}
