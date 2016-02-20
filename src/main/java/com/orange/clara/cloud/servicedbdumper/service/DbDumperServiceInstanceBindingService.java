package com.orange.clara.cloud.servicedbdumper.service;

import com.google.common.collect.Maps;
import com.orange.clara.cloud.servicedbdumper.dbdumper.Credentials;
import com.orange.clara.cloud.servicedbdumper.model.DbDumperCredential;
import com.orange.clara.cloud.servicedbdumper.model.DbDumperServiceInstance;
import com.orange.clara.cloud.servicedbdumper.model.DbDumperServiceInstanceBinding;
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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * Copyright (C) 2015 Orange
 * <p>
 * This software is distributed under the terms and conditions of the 'Apache-2.0'
 * license which can be found in the file 'LICENSE' in this package distribution
 * or at 'https://opensource.org/licenses/Apache-2.0'.
 * <p>
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
    @Qualifier(value = "credentials")
    private Credentials credentials;

    @Autowired
    @Qualifier(value = "dateFormat")
    private String dateFormat;

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

        Map<String, Object> credentials = this.getCredentials(serviceInstanceBinding.getDbDumperServiceInstance());
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
        Map<String, Object> credentials = this.getCredentials(dbDumperServiceInstanceBinding.getDbDumperServiceInstance());

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

    private Map<String, Object> getCredentials(DbDumperServiceInstance dbDumperServiceInstance) {
        return this.extractCredentials(this.credentials.getCredentials(dbDumperServiceInstance));
    }

    private Map<String, Object> extractCredentials(List<DbDumperCredential> dbDumperCredentials) {
        SimpleDateFormat dateFormater = new SimpleDateFormat(this.dateFormat);
        Map<String, Object> credentials = Maps.newHashMap();
        List<Map<String, Object>> dumpFiles = new ArrayList<>();
        Map<String, Object> dumpFile;
        Comparator<DbDumperCredential> comparator = (d1, d2) -> d1.getCreatedAt().compareTo(d2.getCreatedAt());
        dbDumperCredentials.sort(comparator.reversed());
        for (DbDumperCredential dbDumperCredential : dbDumperCredentials) {
            dumpFile = Maps.newHashMap();
            dumpFile.put("download_url", dbDumperCredential.getDownloadUrl());
            dumpFile.put("show_url", dbDumperCredential.getShowUrl());
            dumpFile.put("filename", dbDumperCredential.getFilename());
            dumpFile.put("created_at", dateFormater.format(dbDumperCredential.getCreatedAt()));
            dumpFile.put("dump_id", dbDumperCredential.getId());
            dumpFiles.add(dumpFile);
        }
        credentials.put("dumps", dumpFiles);
        return credentials;
    }
}
