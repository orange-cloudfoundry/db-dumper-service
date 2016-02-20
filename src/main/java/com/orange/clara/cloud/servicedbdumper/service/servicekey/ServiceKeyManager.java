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
public interface ServiceKeyManager {
    CloudServiceKey createServiceKey(String serviceName, String token, String org, String space) throws ServiceKeyException;

    CloudServiceKey createServiceKey(DatabaseService databaseService) throws ServiceKeyException;

    void deleteServiceKey(String guid);

    void deleteServiceKey(DatabaseService databaseService);
}
