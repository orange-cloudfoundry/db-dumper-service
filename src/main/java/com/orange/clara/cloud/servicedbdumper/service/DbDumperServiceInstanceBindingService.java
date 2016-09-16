package com.orange.clara.cloud.servicedbdumper.service;

import com.google.common.collect.Maps;
import com.orange.clara.cloud.servicedbdumper.dbdumper.Credentials;
import com.orange.clara.cloud.servicedbdumper.model.DatabaseRef;
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

import static com.orange.clara.cloud.servicedbdumper.helper.ParameterParser.getParameter;

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
    public final static String SEE_ALL_DUMPS = "see_all_dumps";
    @Autowired
    @Qualifier(value = "dateFormat")
    protected String dateFormat;
    @Autowired
    private DbDumperServiceInstanceBindingRepo repositoryInstanceBinding;
    @Autowired
    private DbDumperServiceInstanceRepo repositoryInstance;
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
        DbDumperServiceInstance dbDumperServiceInstance = repositoryInstance.findOne(request.getServiceInstanceId());
        if (dbDumperServiceInstance == null || dbDumperServiceInstance.isDeleted()) {
            throw new ServiceBrokerException("Cannot find instance: " + request.getServiceInstanceId());
        }

        DbDumperServiceInstanceBinding serviceInstanceBinding = new DbDumperServiceInstanceBinding(
                request.getBindingId(),
                dbDumperServiceInstance,
                request.getAppGuid()
        );
        String seeAllDumps = getParameter(request.getParameters(), SEE_ALL_DUMPS, null);
        Map<String, Object> credentials = null;
        if (seeAllDumps == null || seeAllDumps.equals("false")) {
            credentials = this.getCredentials(serviceInstanceBinding.getDbDumperServiceInstance());
        } else {
            credentials = this.getCredentials(serviceInstanceBinding.getDbDumperServiceInstance().getDatabaseRef());
        }

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

    private Map<String, Object> getCredentials(DatabaseRef databaseRef) {
        return this.extractCredentials(this.credentials.getDumpsCredentials(databaseRef));
    }

    private Map<String, Object> getCredentials(DbDumperServiceInstance dbDumperServiceInstance) {
        return this.extractCredentials(this.credentials.getDumpsCredentials(dbDumperServiceInstance));
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
            dumpFile.put("size", dbDumperCredential.getSize());
            dumpFile.put("deleted", dbDumperCredential.getDeleted());
            dumpFiles.add(dumpFile);
        }
        credentials.put("dumps", dumpFiles);
        return credentials;
    }
}
