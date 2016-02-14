package com.orange.clara.cloud.servicedbdumper.service;

import com.orange.clara.cloud.servicedbdumper.dbdumper.running.Credentials;
import com.orange.clara.cloud.servicedbdumper.model.DbDumperServiceInstanceBinding;
import com.orange.clara.cloud.servicedbdumper.repo.DatabaseDumpFileRepo;
import com.orange.clara.cloud.servicedbdumper.repo.DbDumperServiceInstanceBindingRepo;
import com.orange.clara.cloud.servicedbdumper.repo.DbDumperServiceInstanceRepo;
import org.cloudfoundry.community.servicebroker.exception.ServiceBrokerException;
import org.cloudfoundry.community.servicebroker.exception.ServiceInstanceBindingExistsException;
import org.cloudfoundry.community.servicebroker.model.CreateServiceInstanceBindingRequest;
import org.cloudfoundry.community.servicebroker.model.DeleteServiceInstanceBindingRequest;
import org.cloudfoundry.community.servicebroker.model.ServiceInstanceBinding;
import org.cloudfoundry.community.servicebroker.service.ServiceInstanceBindingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * Copyright (C) 2015 Orange
 * <p/>
 * This software is distributed under the terms and conditions of the 'Apache-2.0'
 * license which can be found in the file 'LICENSE' in this package distribution
 * or at 'https://opensource.org/licenses/Apache-2.0'.
 * <p/>
 * Author: Arthur Halet
 * Date: 12/10/2015
 */
@Service
public class DbDumperServiceInstanceBindingService implements ServiceInstanceBindingService {

    @Autowired
    @Qualifier("appUri")
    private String appUri;
    @Autowired
    private DbDumperServiceInstanceBindingRepo repositoryInstanceBinding;

    @Autowired
    private DbDumperServiceInstanceRepo repositoryInstance;

    @Autowired
    private DatabaseDumpFileRepo databaseDumpFileRepo;

    @Autowired
    @Qualifier(value = "credentials")
    private Credentials credentials;

    @Override
    public ServiceInstanceBinding createServiceInstanceBinding(CreateServiceInstanceBindingRequest request) throws ServiceInstanceBindingExistsException, ServiceBrokerException {
        if (repositoryInstanceBinding.findOne(request.getBindingId()) != null) {
            throw new ServiceInstanceBindingExistsException(
                    new ServiceInstanceBinding(
                            request.getBindingId(),
                            request.getServiceInstanceId(),
                            request.getParameters(),
                            "",
                            request.getAppGuid()
                    ));
        }
        if (repositoryInstance.findOne(request.getServiceInstanceId()) == null) {
            throw new ServiceBrokerException("Cannot find instance: " + request.getServiceInstanceId());
        }
        DbDumperServiceInstanceBinding serviceInstanceBinding = new DbDumperServiceInstanceBinding(
                request.getBindingId(),
                repositoryInstance.findOne(request.getServiceInstanceId()),
                request.getAppGuid()
        );

        Map<String, Object> credentials = this.credentials.getCredentials(serviceInstanceBinding.getDbDumperServiceInstance());
        repositoryInstanceBinding.save(serviceInstanceBinding);
        return new ServiceInstanceBinding(
                request.getBindingId(),
                request.getServiceInstanceId(),
                credentials,
                null,
                request.getAppGuid()
        );
    }

    @Override
    public ServiceInstanceBinding deleteServiceInstanceBinding(DeleteServiceInstanceBindingRequest request) throws ServiceBrokerException {
        DbDumperServiceInstanceBinding dbDumperServiceInstanceBinding = repositoryInstanceBinding.findOne(request.getBindingId());
        if (dbDumperServiceInstanceBinding == null) {
            throw new ServiceBrokerException("Cannot find binding instance: " + request.getBindingId());
        }
        Map<String, Object> credentials = this.credentials.getCredentials(dbDumperServiceInstanceBinding.getDbDumperServiceInstance());

        ServiceInstanceBinding serviceInstanceBinding = new ServiceInstanceBinding(
                dbDumperServiceInstanceBinding.getId(),
                dbDumperServiceInstanceBinding.getDbDumperServiceInstance().getServiceInstanceId(),
                credentials,
                null,
                dbDumperServiceInstanceBinding.getAppGuid()
        );
        repositoryInstanceBinding.delete(dbDumperServiceInstanceBinding);
        return serviceInstanceBinding;
    }
}
