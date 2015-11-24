package com.orange.clara.cloud.servicedbdumper.service;

import com.google.common.collect.Maps;
import com.orange.clara.cloud.servicedbdumper.model.DatabaseDumpFile;
import com.orange.clara.cloud.servicedbdumper.model.DbDumperServiceInstanceBinding;
import com.orange.clara.cloud.servicedbdumper.repo.DatabaseDumpFileRepo;
import com.orange.clara.cloud.servicedbdumper.repo.DbDumperServiceInstanceBindingRepository;
import com.orange.clara.cloud.servicedbdumper.repo.DbDumperServiceInstanceRepository;
import org.cloudfoundry.community.servicebroker.exception.ServiceBrokerException;
import org.cloudfoundry.community.servicebroker.exception.ServiceInstanceBindingExistsException;
import org.cloudfoundry.community.servicebroker.model.CreateServiceInstanceBindingRequest;
import org.cloudfoundry.community.servicebroker.model.DeleteServiceInstanceBindingRequest;
import org.cloudfoundry.community.servicebroker.model.ServiceInstanceBinding;
import org.cloudfoundry.community.servicebroker.service.ServiceInstanceBindingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.Map;

/**
 * Copyright (C) 2015 Orange
 * <p/>
 * This software is distributed under the terms and conditions of the 'MIT'
 * license which can be found in the file 'LICENSE' in this package distribution
 * or at 'http://opensource.org/licenses/MIT'.
 * <p/>
 * Author: Arthur Halet
 * Date: 12/10/2015
 */
@Service
public class DbDumperServiceInstanceBindingService implements ServiceInstanceBindingService {

    private final static String DASHBOARD_URL = "/manage";
    private final static String DOWNLOAD_URL = "/list/%s/%s";
    private final static String RAW_URL = "/list/%s/%s";
    private final static String LIST_BY_INSTANCE_URL = "/list/%s";

    @Value("${vcap.application.uris[0]:localhost:8080}")
    private String appUri;
    @Autowired
    private DbDumperServiceInstanceBindingRepository repositoryInstanceBinding;

    @Autowired
    private DbDumperServiceInstanceRepository repositoryInstance;

    @Autowired
    private DatabaseDumpFileRepo databaseDumpFileRepo;

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
        if (repositoryInstance.findOne(request.getServiceInstanceId()) != null) {
            throw new ServiceBrokerException("Cannot find instance: " + request.getServiceInstanceId());
        }
        DbDumperServiceInstanceBinding serviceInstanceBinding = new DbDumperServiceInstanceBinding(
                request.getBindingId(),
                repositoryInstance.findOne(request.getServiceInstanceId()),
                request.getAppGuid()
        );

        Map<String, Object> credentials = this.getCredentials(serviceInstanceBinding);
        serviceInstanceBinding.setCredentials(credentials);
        repositoryInstanceBinding.save(serviceInstanceBinding);
        return new ServiceInstanceBinding(
                request.getBindingId(),
                request.getServiceInstanceId(),
                credentials,
                "",
                request.getAppGuid()
        );
    }

    @Override
    public ServiceInstanceBinding deleteServiceInstanceBinding(DeleteServiceInstanceBindingRequest request) throws ServiceBrokerException {
        DbDumperServiceInstanceBinding dbDumperServiceInstanceBinding = repositoryInstanceBinding.findOne(request.getBindingId());
        if (repositoryInstance.findOne(request.getBindingId()) != null) {
            throw new ServiceBrokerException("Cannot find binding instance: " + request.getBindingId());
        }
        ServiceInstanceBinding serviceInstanceBinding = new ServiceInstanceBinding(
                dbDumperServiceInstanceBinding.getId(),
                dbDumperServiceInstanceBinding.getDbDumperServiceInstance().getServiceInstanceId(),
                dbDumperServiceInstanceBinding.getCredentials(),
                "",
                dbDumperServiceInstanceBinding.getAppGuid()
        );
        repositoryInstanceBinding.delete(dbDumperServiceInstanceBinding);
        return serviceInstanceBinding;
    }

    public Map<String, Object> getCredentials(DbDumperServiceInstanceBinding serviceInstanceBinding) {
        Map<String, Object> credentials = Maps.newHashMap();
        DatabaseDumpFile latestDatabaseDumpFile = null;
        String fileName = "";
        if (serviceInstanceBinding.getDbDumperServiceInstance() == null || serviceInstanceBinding.getDbDumperServiceInstance().getDatabaseRef() == null) {
            return credentials;
        }

        latestDatabaseDumpFile = this.databaseDumpFileRepo.findFirstByDatabaseRefOrderByCreatedAtDesc(serviceInstanceBinding.getDbDumperServiceInstance().getDatabaseRef());

        if (latestDatabaseDumpFile != null) {
            fileName = latestDatabaseDumpFile.getFileName();
        }
        credentials.put("latest_file",
                String.format(
                        "http://" + this.appUri + DOWNLOAD_URL,
                        serviceInstanceBinding.getDbDumperServiceInstance().getDatabaseRef().getDatabaseName(),
                        fileName
                )
        );
        credentials.put("latest_raw_file",
                String.format(
                        "http://" + this.appUri + RAW_URL,
                        serviceInstanceBinding.getDbDumperServiceInstance().getDatabaseRef().getDatabaseName(),
                        fileName
                )
        );
        credentials.put("list_url",
                String.format(
                        "http://" + this.appUri + LIST_BY_INSTANCE_URL,
                        serviceInstanceBinding.getDbDumperServiceInstance().getServiceInstanceId()
                )
        );
        credentials.put("dashboard_url", "http://" + this.appUri + DASHBOARD_URL);

        SimpleDateFormat form = new SimpleDateFormat("dd-MM-yyyy");
        for (DatabaseDumpFile databaseDumpFile : serviceInstanceBinding.getDbDumperServiceInstance().getDatabaseRef().getDatabaseDumpFiles()) {

            credentials.put(form.format(databaseDumpFile.getCreatedAt()) + "_file",
                    String.format(
                            "http://" + this.appUri + DOWNLOAD_URL,
                            serviceInstanceBinding.getDbDumperServiceInstance().getDatabaseRef().getDatabaseName(),
                            databaseDumpFile.getFileName()
                    )
            );
            credentials.put(form.format(databaseDumpFile.getCreatedAt()) + "_raw_file",
                    String.format(
                            "http://" + this.appUri + RAW_URL,
                            serviceInstanceBinding.getDbDumperServiceInstance().getDatabaseRef().getDatabaseName(),
                            databaseDumpFile.getFileName()
                    )
            );
        }
        return credentials;
    }

}
