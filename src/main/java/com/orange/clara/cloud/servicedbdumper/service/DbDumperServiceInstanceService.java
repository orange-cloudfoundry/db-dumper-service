package com.orange.clara.cloud.servicedbdumper.service;

import com.orange.clara.cloud.servicedbdumper.dbdump.action.Dumper;
import com.orange.clara.cloud.servicedbdumper.dbdump.action.Restorer;
import com.orange.clara.cloud.servicedbdumper.exception.RestoreCannotFindFile;
import com.orange.clara.cloud.servicedbdumper.exception.RestoreException;
import com.orange.clara.cloud.servicedbdumper.model.DatabaseRef;
import com.orange.clara.cloud.servicedbdumper.model.DbDumperServiceInstance;
import com.orange.clara.cloud.servicedbdumper.model.UpdateAction;
import com.orange.clara.cloud.servicedbdumper.repo.DatabaseRefRepo;
import com.orange.clara.cloud.servicedbdumper.repo.DbDumperServiceInstanceRepository;
import org.cloudfoundry.community.servicebroker.exception.ServiceBrokerException;
import org.cloudfoundry.community.servicebroker.exception.ServiceInstanceDoesNotExistException;
import org.cloudfoundry.community.servicebroker.exception.ServiceInstanceExistsException;
import org.cloudfoundry.community.servicebroker.exception.ServiceInstanceUpdateNotSupportedException;
import org.cloudfoundry.community.servicebroker.model.CreateServiceInstanceRequest;
import org.cloudfoundry.community.servicebroker.model.DeleteServiceInstanceRequest;
import org.cloudfoundry.community.servicebroker.model.ServiceInstance;
import org.cloudfoundry.community.servicebroker.model.UpdateServiceInstanceRequest;
import org.cloudfoundry.community.servicebroker.service.ServiceInstanceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

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
public class DbDumperServiceInstanceService implements ServiceInstanceService {

    private final static String SRC_URL_PARAMETER = "src_url";
    private final static String ACTION_PARAMETER = "action";
    private final static String CREATED_AT_PARAMETER = "created_at";
    private final static String TARGET_URL_PARAMETER = "target_url";
    private final static String DASHBOARD_ROUTE = "/dashboard";


    @Autowired
    @Qualifier(value = "restorer")
    private Restorer restorer;
    @Autowired
    private DbDumperServiceInstanceRepository repository;

    @Value("${vcap.application.uris[0]:localhost:8080}")
    private String appUri;

    @Autowired
    @Qualifier(value = "dumper")
    private Dumper dumper;
    @Autowired
    private DatabaseRefRepo databaseRefRepo;

    @Override
    public ServiceInstance createServiceInstance(CreateServiceInstanceRequest request) throws ServiceInstanceExistsException, ServiceBrokerException {
        if (repository.findOne(request.getServiceInstanceId()) != null) {
            throw new ServiceInstanceExistsException(new ServiceInstance(request));
        }
        DbDumperServiceInstance dbDumperServiceInstance = new DbDumperServiceInstance(
                request.getServiceInstanceId(),
                request.getPlanId(),
                request.getOrganizationGuid(),
                request.getSpaceGuid(),
                "http://" + appUri + DASHBOARD_ROUTE);
        this.createDump(request.getParameters(), dbDumperServiceInstance);
        repository.save(dbDumperServiceInstance);
        return new ServiceInstance(request);
    }

    @Override
    public ServiceInstance getServiceInstance(String s) {
        DbDumperServiceInstance instance = repository.findOne(s);
        if (instance != null) {
            return new ServiceInstance(new CreateServiceInstanceRequest().withServiceInstanceId(s));
        }
        return null;
    }

    @Override
    public ServiceInstance deleteServiceInstance(DeleteServiceInstanceRequest deleteServiceInstanceRequest) throws ServiceBrokerException {
        return new ServiceInstance(deleteServiceInstanceRequest);
    }

    @Override
    public ServiceInstance updateServiceInstance(UpdateServiceInstanceRequest request) throws ServiceInstanceUpdateNotSupportedException, ServiceBrokerException, ServiceInstanceDoesNotExistException {
        DbDumperServiceInstance instance = repository.findOne(request.getServiceInstanceId());
        ServiceInstance serviceInstance = new ServiceInstance(request);
        if (instance == null) {
            throw new ServiceInstanceDoesNotExistException(request.getServiceInstanceId());
        }
        Map<String, Object> parameters = request.getParameters();
        UpdateAction action = null;
        try {
            action = UpdateAction.valueOf(parameters.get(ACTION_PARAMETER).toString().toUpperCase());
        } catch (Exception e) {
            throw new ServiceBrokerException("Action doesn't exist. you need to set this parameter: " + ACTION_PARAMETER + " valid value are: " + UpdateAction.showValues());
        }
        if (action.equals(UpdateAction.DUMP)) {
            this.createDump(request.getParameters(), instance);
        } else if (action.equals(UpdateAction.RESTORE)) {
            try {
                this.restoreDump(request.getParameters(), instance);
            } catch (RestoreCannotFindFile e) {
                throw new ServiceBrokerException(e.getMessage());
            } catch (RestoreException e) {
                throw new ServiceBrokerException("An error occured during restore: " + e.getMessage(), e);
            }
        }
        return serviceInstance;
    }

    private void createDump(Map<String, Object> parameters, DbDumperServiceInstance dbDumperServiceInstance) throws ServiceBrokerException {

        String srcUrl = this.getParameter(parameters, SRC_URL_PARAMETER);
        UUID dbRefName = UUID.nameUUIDFromBytes(srcUrl.getBytes());
        DatabaseRef databaseRef = this.getDatabaseRefFromUrl(srcUrl, dbRefName.toString());
        dbDumperServiceInstance.setDatabaseRef(databaseRef);
        try {
            dumper.dump(databaseRef);
        } catch (Exception e) {
            throw new ServiceBrokerException("An error occurred during dump: " + e.getMessage(), e);
        }

    }

    private String getParameter(Map<String, Object> parameters, String parameter) throws ServiceBrokerException {
        if (parameters == null) {
            throw new ServiceBrokerException("You need to set " + parameter + " parameter.");
        }
        Object paramObject = parameters.get(parameter);
        if (paramObject == null || paramObject.toString().isEmpty()) {
            throw new ServiceBrokerException("You need to set " + parameter + " parameter.");
        }
        return paramObject.toString();
    }

    private void restoreDump(Map<String, Object> parameters, DbDumperServiceInstance dbDumperServiceInstance) throws ServiceBrokerException, RestoreException {
        String srcUrl = this.getParameter(parameters, SRC_URL_PARAMETER);
        String targetUrl = this.getParameter(parameters, TARGET_URL_PARAMETER);
        String createdAtString = null;
        try {
            createdAtString = this.getParameter(parameters, CREATED_AT_PARAMETER);
        } catch (ServiceBrokerException e) {
        }

        UUID dbSrcName = UUID.nameUUIDFromBytes(srcUrl.getBytes());
        UUID dbTargetName = UUID.nameUUIDFromBytes(targetUrl.getBytes());

        DatabaseRef databaseRefSource = this.getDatabaseRefFromUrl(srcUrl, dbSrcName.toString());
        DatabaseRef databaseRefTarget = this.getDatabaseRefFromUrl(targetUrl, dbTargetName.toString());
        dbDumperServiceInstance.setDatabaseRef(databaseRefSource);
        if (createdAtString == null || createdAtString.isEmpty()) {
            this.restorer.restore(databaseRefSource, databaseRefTarget);
            return;
        }
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Date createdAt;
        try {
            createdAt = simpleDateFormat.parse(createdAtString);
        } catch (ParseException e) {
            throw new ServiceBrokerException("When use " + CREATED_AT_PARAMETER + " parameter you should pass a date in this form: yyyy-MM-dd");
        }
        this.restorer.restore(databaseRefSource, databaseRefTarget, createdAt);
    }

    private DatabaseRef getDatabaseRefFromUrl(String dbUrl, String serviceName) {
        DatabaseRef databaseRef = new DatabaseRef(serviceName, URI.create(dbUrl));
        DatabaseRef databaseRefDao = null;
        if (!this.databaseRefRepo.exists(serviceName)) {
            this.databaseRefRepo.save(databaseRef);
            return databaseRef;
        }
        databaseRefDao = this.databaseRefRepo.findOne(serviceName);
        this.updateDatabaseRef(databaseRef, databaseRefDao);
        databaseRef = databaseRefDao;

        return databaseRef;
    }

    private void updateDatabaseRef(DatabaseRef databaseRefTemp, DatabaseRef databaseRefDao) {
        if (databaseRefDao.equals(databaseRefTemp)) {
            return;
        }
        databaseRefDao.setDatabaseName(databaseRefTemp.getDatabaseName());
        databaseRefDao.setHost(databaseRefTemp.getHost());
        databaseRefDao.setPassword(databaseRefTemp.getPassword());
        databaseRefDao.setPort(databaseRefTemp.getPort());
        databaseRefDao.setType(databaseRefTemp.getType());
        databaseRefDao.setUser(databaseRefTemp.getUser());
        this.databaseRefRepo.save(databaseRefDao);
    }
}
