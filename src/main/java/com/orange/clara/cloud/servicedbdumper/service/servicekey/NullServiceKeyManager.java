package com.orange.clara.cloud.servicedbdumper.service.servicekey;

import com.orange.clara.cloud.servicedbdumper.exception.ServiceKeyException;
import com.orange.clara.cloud.servicedbdumper.model.DatabaseService;
import org.cloudfoundry.client.lib.domain.CloudServiceKey;

/**
 * Copyright (C) 2016 Orange
 * <p>
 * This software is distributed under the terms and conditions of the 'Apache-2.0'
 * license which can be found in the file 'LICENSE' in this package distribution
 * or at 'https://opensource.org/licenses/Apache-2.0'.
 * <p>
 * Author: Arthur Halet
 * Date: 19/02/2016
 */
public class NullServiceKeyManager implements ServiceKeyManager {
    @Override
    public CloudServiceKey createServiceKey(String serviceName, String token, String org, String space) throws ServiceKeyException {
        this.throwError();
        return null;
    }

    @Override
    public CloudServiceKey createServiceKey(DatabaseService databaseService) throws ServiceKeyException {
        this.throwError();
        return null;
    }

    @Override
    public void deleteServiceKey(String guid) {

    }

    @Override
    public void deleteServiceKey(DatabaseService databaseService) {

    }

    private void throwError() throws ServiceKeyException {
        throw new ServiceKeyException("You can't pass a service name if you don't provide a cloudfoundry " +
                "admin user and password and the url of cloudfoundry api for db-dumper-service." +
                "(see env var: cf_admin_user, cf_admin_password, cloud_controller_url )");
    }
}
