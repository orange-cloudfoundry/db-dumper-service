package com.orange.clara.cloud.servicedbdumper.service;

import com.orange.clara.cloud.servicedbdumper.dbdumper.running.Dumper;
import com.orange.clara.cloud.servicedbdumper.dbdumper.running.Restorer;
import com.orange.clara.cloud.servicedbdumper.exception.RestoreCannotFindFile;
import com.orange.clara.cloud.servicedbdumper.exception.RestoreException;
import com.orange.clara.cloud.servicedbdumper.model.DatabaseRef;
import com.orange.clara.cloud.servicedbdumper.model.DbDumperServiceInstance;
import com.orange.clara.cloud.servicedbdumper.model.Job;
import com.orange.clara.cloud.servicedbdumper.model.UpdateAction;
import com.orange.clara.cloud.servicedbdumper.repo.DatabaseRefRepo;
import com.orange.clara.cloud.servicedbdumper.repo.DbDumperServiceInstanceBindingRepo;
import com.orange.clara.cloud.servicedbdumper.repo.DbDumperServiceInstanceRepo;
import com.orange.clara.cloud.servicedbdumper.repo.JobRepo;
import com.orange.clara.cloud.servicedbdumper.task.job.JobFactory;
import org.cloudfoundry.community.servicebroker.exception.ServiceBrokerException;
import org.cloudfoundry.community.servicebroker.exception.ServiceInstanceDoesNotExistException;
import org.cloudfoundry.community.servicebroker.exception.ServiceInstanceExistsException;
import org.cloudfoundry.community.servicebroker.exception.ServiceInstanceUpdateNotSupportedException;
import org.cloudfoundry.community.servicebroker.model.*;
import org.cloudfoundry.community.servicebroker.service.ServiceInstanceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    private final static String DASHBOARD_ROUTE = "/manage";
    private Logger logger = LoggerFactory.getLogger(DbDumperServiceInstanceService.class);

    @Autowired
    @Qualifier(value = "dateFormat")
    private String dateFormat;


    @Autowired
    @Qualifier(value = "restorer")
    private Restorer restorer;
    @Autowired
    private DbDumperServiceInstanceRepo repository;

    @Autowired
    private DbDumperServiceInstanceBindingRepo serviceInstanceBindingRepo;

    @Autowired
    @Qualifier("appUri")
    private String appUri;

    @Autowired
    @Qualifier("jobFactory")
    private JobFactory jobFactory;

    @Autowired
    private JobRepo jobRepo;

    @Autowired
    @Qualifier(value = "dumper")
    private Dumper dumper;
    @Autowired
    private DatabaseRefRepo databaseRefRepo;

    @Override
    public ServiceInstance createServiceInstance(CreateServiceInstanceRequest request) throws ServiceInstanceExistsException, ServiceBrokerException {
        DbDumperServiceInstance dbDumperServiceInstance = repository.findOne(request.getServiceInstanceId());
        if (dbDumperServiceInstance != null) {
            throw new ServiceInstanceExistsException(new ServiceInstance(request));
        }
        dbDumperServiceInstance = new DbDumperServiceInstance(
                request.getServiceInstanceId(),
                request.getPlanId(),
                request.getOrganizationGuid(),
                request.getSpaceGuid(),
                appUri + DASHBOARD_ROUTE);
        this.createDump(request.getParameters(), dbDumperServiceInstance);
        return new ServiceInstance(request).withDashboardUrl(appUri + DASHBOARD_ROUTE).withAsync(true);
    }

    @Override
    public ServiceInstance getServiceInstance(String s) {
        DbDumperServiceInstance instance = repository.findOne(s);
        if (instance == null) {
            return null;
        }
        ServiceInstanceLastOperation serviceInstanceLastOperation = null;

        Job lastJob = this.jobRepo.findFirstByDbDumperServiceInstanceOrderByUpdatedAtDesc(instance);

        switch (lastJob.getJobEvent()) {
            case ERRORED:
                serviceInstanceLastOperation = new ServiceInstanceLastOperation("Error: " + lastJob.getErrorMessage(), OperationState.FAILED);
                break;
            case START:
            case SCHEDULED:
            case RUNNING:
                String description = String.format("Job of type '%s' for instance '%s' is '%s'\n", lastJob.getJobType(), instance.getServiceInstanceId(), lastJob.getJobEvent());
                serviceInstanceLastOperation = new ServiceInstanceLastOperation(description, OperationState.IN_PROGRESS);
                break;
            default:
                serviceInstanceLastOperation = new ServiceInstanceLastOperation("Finished", OperationState.SUCCEEDED);
        }
        return new ServiceInstance(new CreateServiceInstanceRequest().withServiceInstanceId(s)).withAsync(true).withDashboardUrl(appUri + DASHBOARD_ROUTE).withLastOperation(serviceInstanceLastOperation);
    }

    @Override
    @Transactional
    public ServiceInstance deleteServiceInstance(DeleteServiceInstanceRequest request) throws ServiceBrokerException {
        DbDumperServiceInstance dbDumperServiceInstance = repository.findOne(request.getServiceInstanceId());
        if (dbDumperServiceInstance == null) {
            return new ServiceInstance(request);
        }
        this.jobRepo.deleteByDbDumperServiceInstance(dbDumperServiceInstance);
        DatabaseRef databaseRef = dbDumperServiceInstance.getDatabaseRef();
        databaseRef.removeDbDumperServiceInstance(dbDumperServiceInstance);
        this.databaseRefRepo.save(databaseRef);
        this.serviceInstanceBindingRepo.deleteByDbDumperServiceInstance(dbDumperServiceInstance);
        repository.delete(dbDumperServiceInstance);
        if (databaseRef.getDbDumperServiceInstances().size() == 0) {
            databaseRef.setDeleted(true);
        }
        this.jobFactory.createJobDeleteDatabaseRef(databaseRef);
        return new ServiceInstance(request).withDashboardUrl(appUri + DASHBOARD_ROUTE).withAsync(false);
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
            this.createDump(instance);
        } else if (action.equals(UpdateAction.RESTORE)) {
            try {
                this.restoreDump(request.getParameters(), instance);
            } catch (RestoreCannotFindFile e) {
                throw new ServiceBrokerException(e.getMessage());
            } catch (RestoreException e) {
                throw new ServiceBrokerException("An error occurred during restore: " + e.getMessage(), e);
            }
        }
        serviceInstance.withDashboardUrl(appUri + DASHBOARD_ROUTE).withAsync(true);
        return serviceInstance;
    }

    private void createDump(DbDumperServiceInstance dbDumperServiceInstance) throws ServiceBrokerException {
        this.jobFactory.createJobCreateDump(dbDumperServiceInstance);
    }

    private void createDump(Map<String, Object> parameters, DbDumperServiceInstance dbDumperServiceInstance) throws ServiceBrokerException {

        String srcUrl = this.getParameter(parameters, SRC_URL_PARAMETER);
        UUID dbRefName = UUID.nameUUIDFromBytes(srcUrl.getBytes());
        DatabaseRef databaseRef = this.getDatabaseRefFromUrl(srcUrl, dbRefName.toString());
        if (databaseRef.isDeleted()) {
            databaseRef.setDeleted(false);
            databaseRefRepo.save(databaseRef);
        }
        dbDumperServiceInstance.setDatabaseRef(databaseRef);
        repository.save(dbDumperServiceInstance);
        this.createDump(dbDumperServiceInstance);
    }

    private String getParameter(Map<String, Object> parameters, String parameter) throws ServiceBrokerException {
        String param = this.getParameter(parameters, parameter, null);
        if (param == null) {
            throw new ServiceBrokerException("You need to set " + parameter + " parameter.");
        }
        return param;
    }

    private String getParameter(Map<String, Object> parameters, String parameter, String defaultValue) throws ServiceBrokerException {
        if (parameters == null) {
            return defaultValue;
        }
        Object paramObject = parameters.get(parameter);
        if (paramObject == null) {
            return defaultValue;
        }
        return paramObject.toString();
    }

    private void restoreDump(Map<String, Object> parameters, DbDumperServiceInstance dbDumperServiceInstance) throws ServiceBrokerException, RestoreException {
        String targetUrl = this.getParameter(parameters, TARGET_URL_PARAMETER);
        String createdAtString = this.getParameter(parameters, CREATED_AT_PARAMETER, null);

        UUID dbTargetName = UUID.nameUUIDFromBytes(targetUrl.getBytes());
        DatabaseRef databaseRefTarget = this.getDatabaseRefFromUrl(targetUrl, dbTargetName.toString());
        if (createdAtString == null || createdAtString.isEmpty()) {
            SimpleDateFormat form = new SimpleDateFormat(this.dateFormat);
            Date today = new Date();
            try {
                today = form.parse(form.format(new Date()));
            } catch (ParseException e) { // should have no error
            }
            this.jobFactory.createJobRestoreDump(databaseRefTarget, today, dbDumperServiceInstance);

            return;
        }
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(this.dateFormat);
        Date createdAt;
        try {
            createdAt = simpleDateFormat.parse(createdAtString);
        } catch (ParseException e) {
            throw new ServiceBrokerException("When use " + CREATED_AT_PARAMETER + " parameter you should pass a date in this form: " + this.dateFormat);
        }
        this.jobFactory.createJobRestoreDump(databaseRefTarget, createdAt, dbDumperServiceInstance);

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
