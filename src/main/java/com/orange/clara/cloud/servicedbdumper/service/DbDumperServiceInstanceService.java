package com.orange.clara.cloud.servicedbdumper.service;

import com.orange.clara.cloud.servicedbdumper.config.Routes;
import com.orange.clara.cloud.servicedbdumper.dbdumper.DatabaseRefManager;
import com.orange.clara.cloud.servicedbdumper.exception.DatabaseExtractionException;
import com.orange.clara.cloud.servicedbdumper.exception.RestoreCannotFindFileException;
import com.orange.clara.cloud.servicedbdumper.exception.RestoreException;
import com.orange.clara.cloud.servicedbdumper.exception.ServiceKeyException;
import com.orange.clara.cloud.servicedbdumper.model.*;
import com.orange.clara.cloud.servicedbdumper.repo.*;
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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
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
public class DbDumperServiceInstanceService implements ServiceInstanceService {

    public final static String NEW_SRC_URL_PARAMETER = "db";
    public final static String SRC_URL_PARAMETER = "src_url";
    public final static String ACTION_PARAMETER = "action";
    public final static String CREATED_AT_PARAMETER = "created_at";
    public final static String NEW_TARGET_URL_PARAMETER = "db";
    public final static String TARGET_URL_PARAMETER = "target_url";
    public final static String CF_USER_TOKEN_PARAMETER = "cf_user_token";
    public final static String ORG_PARAMETER = "org";
    public final static String SPACE_PARAMETER = "space";


    private final static String DASHBOARD_ROUTE = Routes.MANAGE_ROOT + Routes.MANAGE_LIST_DATABASE_ROOT + "/";
    private final static String[] VALID_DATES_FORMAT = {
            "dd-MM-yyyy HH:mm:ss",
            "dd/MM/yyyy HH:mm:ss",
            "yyyy/MM/dd HH:mm:ss",
            "dd-MM-yyyy HH:mm",
            "dd/MM/yyyy HH:mm",
            "yyyy/MM/dd HH:mm",
            "dd-MM-yyyy HH",
            "dd/MM/yyyy HH",
            "yyyy/MM/dd HH",
            "dd-MM-yyyy",
            "dd/MM/yyyy",
            "yyyy/MM/dd",
            "MM/yyyy",
            "MM-yyyy",
            "yyyy/MM"
    };
    @Autowired
    @Qualifier("appUri")
    protected String appUri;

    private Logger logger = LoggerFactory.getLogger(DbDumperServiceInstanceService.class);

    @Autowired
    private DatabaseRefManager databaseRefManager;

    @Autowired
    private DbDumperServiceInstanceRepo repository;

    @Autowired
    private DbDumperServiceInstanceBindingRepo serviceInstanceBindingRepo;

    @Autowired
    @Qualifier("jobFactory")
    private JobFactory jobFactory;

    @Autowired
    private JobRepo jobRepo;

    @Autowired
    private DatabaseRefRepo databaseRefRepo;

    @Autowired
    private DbDumperPlanRepo dbDumperPlanRepo;

    @Override
    public ServiceInstance createServiceInstance(CreateServiceInstanceRequest request) throws ServiceInstanceExistsException, ServiceBrokerException {
        DbDumperServiceInstance dbDumperServiceInstance = repository.findOne(request.getServiceInstanceId());
        if (dbDumperServiceInstance != null) {
            throw new ServiceInstanceExistsException(new ServiceInstance(request));
        }
        DbDumperPlan dbDumperPlan = dbDumperPlanRepo.findOne(request.getPlanId());
        if (dbDumperPlan == null) {
            throw new ServiceBrokerException("Plan '" + request.getPlanId() + "' is not available.");
        }
        dbDumperServiceInstance = new DbDumperServiceInstance(
                request.getServiceInstanceId(),
                request.getPlanId(),
                request.getOrganizationGuid(),
                request.getSpaceGuid(),
                appUri + DASHBOARD_ROUTE,
                dbDumperPlan);
        this.createDump(request.getParameters(), dbDumperServiceInstance);
        return new ServiceInstance(request).withDashboardUrl(appUri + DASHBOARD_ROUTE + dbDumperServiceInstance.getDatabaseRef().getName()).withAsync(true);
    }

    @Override
    public ServiceInstance getServiceInstance(String serviceInstanceId) {
        DbDumperServiceInstance instance = repository.findOne(serviceInstanceId);
        if (instance == null) {
            return null;
        }
        ServiceInstanceLastOperation serviceInstanceLastOperation = null;

        Job lastJob = this.jobRepo.findFirstByDbDumperServiceInstanceOrderByUpdatedAtDesc(instance);
        ServiceInstance serviceInstance = new ServiceInstance(new CreateServiceInstanceRequest().withServiceInstanceId(serviceInstanceId))
                .withDashboardUrl(appUri + DASHBOARD_ROUTE + instance.getDatabaseRef().getName());
        if (lastJob == null) {
            return serviceInstance;
        }
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
                break;
        }
        return serviceInstance.withAsync(true).withLastOperation(serviceInstanceLastOperation);
    }

    @Override
    @Transactional
    public ServiceInstance deleteServiceInstance(DeleteServiceInstanceRequest request) throws ServiceBrokerException {
        DbDumperServiceInstance dbDumperServiceInstance = repository.findOne(request.getServiceInstanceId());
        if (dbDumperServiceInstance == null) {
            logger.warn("The service instance '" + request.getServiceInstanceId() + "' doesn't exist. Defaulting to say to cloud controller that instance is deleted.");
            return new ServiceInstance(request);
        }
        String dbRefName = dbDumperServiceInstance.getDatabaseRef().getName();
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
        return new ServiceInstance(request).withDashboardUrl(appUri + DASHBOARD_ROUTE + dbRefName).withAsync(false);
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
            this.createDumpFromdbDumperServiceInstance(instance);
        } else if (action.equals(UpdateAction.RESTORE)) {
            try {
                this.restoreDump(request.getParameters(), instance);
            } catch (RestoreCannotFindFileException e) {
                throw new ServiceBrokerException(e.getMessage());
            } catch (RestoreException e) {
                throw new ServiceBrokerException("An error occurred during restore: " + e.getMessage(), e);
            }
        }
        serviceInstance.withDashboardUrl(appUri + DASHBOARD_ROUTE + instance.getDatabaseRef().getName()).withAsync(true);
        return serviceInstance;
    }

    private void createDumpFromdbDumperServiceInstance(DbDumperServiceInstance dbDumperServiceInstance) throws ServiceInstanceDoesNotExistException, ServiceBrokerException {
        if (dbDumperServiceInstance.getDatabaseRef() == null) {
            throw new ServiceInstanceDoesNotExistException("There is no database set for this instance");
        }
        try {
            dbDumperServiceInstance.setDatabaseRef(this.databaseRefManager.updateDatabaseRef(dbDumperServiceInstance.getDatabaseRef()));
        } catch (ServiceKeyException | DatabaseExtractionException e) {
            throw new ServiceBrokerException("An error occurred during dump: " + e.getMessage(), e);
        }
        this.jobFactory.createJobCreateDump(dbDumperServiceInstance);
    }

    private void createDump(Map<String, Object> parameters, DbDumperServiceInstance dbDumperServiceInstance) throws ServiceBrokerException {
        String srcUrl = this.getParameter(parameters, SRC_URL_PARAMETER, null);
        if (srcUrl == null) {
            srcUrl = this.getParameter(parameters, NEW_SRC_URL_PARAMETER);
        }
        DatabaseRef databaseRef = this.getDatabaseRefFromParams(parameters, srcUrl);
        if (databaseRef.isDeleted()) {
            databaseRef.setDeleted(false);
            databaseRefRepo.save(databaseRef);
        }
        dbDumperServiceInstance.setDatabaseRef(databaseRef);
        repository.save(dbDumperServiceInstance);
        this.jobFactory.createJobCreateDump(dbDumperServiceInstance);
    }

    private String getParameter(Map<String, Object> parameters, String parameter) throws ServiceBrokerException {
        String param = this.getParameter(parameters, parameter, null);
        if (param == null) {
            throw new ServiceBrokerException("You need to set '" + parameter + "' parameter.");
        }
        return param;
    }

    private DatabaseRef getDatabaseRefFromParams(Map<String, Object> parameters, String dbUrlOrService) throws ServiceBrokerException {
        String token = this.getParameter(parameters, CF_USER_TOKEN_PARAMETER, null);
        String org = this.getParameter(parameters, ORG_PARAMETER, null);
        String space = this.getParameter(parameters, SPACE_PARAMETER, null);
        try {
            return this.databaseRefManager.getDatabaseRef(dbUrlOrService, token, org, space);
        } catch (ServiceKeyException | DatabaseExtractionException e) {
            throw new ServiceBrokerException("Error when getting database: " + e.getMessage(), e);
        }
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
        String targetUrl = this.getParameter(parameters, TARGET_URL_PARAMETER, null);
        if (targetUrl == null) {
            targetUrl = this.getParameter(parameters, NEW_TARGET_URL_PARAMETER);
        }
        String createdAtString = this.getParameter(parameters, CREATED_AT_PARAMETER, null);
        DatabaseRef databaseRefTarget = this.getDatabaseRefFromParams(parameters, targetUrl);
        /*try {
         dbDumperServiceInstance.setDatabaseRef(this.databaseRefManager.updateDatabaseRef(dbDumperServiceInstance.getDatabaseRef()));
         } catch (ServiceKeyException | DatabaseExtractionException e) {
         throw new ServiceBrokerException("An error occurred during restore: " + e.getMessage(), e);
         }*/
        if (createdAtString == null || createdAtString.isEmpty()) {
            this.jobFactory.createJobRestoreDump(databaseRefTarget, null, dbDumperServiceInstance);
            return;
        }
        Date createdAt;
        try {
            createdAt = this.parseDate(createdAtString);
        } catch (ParseException e) {
            throw new ServiceBrokerException("When use " + CREATED_AT_PARAMETER + " parameter you should pass a date in one of this forms: " + String.join(", ", VALID_DATES_FORMAT));
        }
        this.jobFactory.createJobRestoreDump(databaseRefTarget, createdAt, dbDumperServiceInstance);
    }


    protected Date parseDate(String date) throws ParseException {
        SimpleDateFormat simpleDateFormat;
        Date createdAt = null;
        for (String validDateFormat : VALID_DATES_FORMAT) {
            simpleDateFormat = new SimpleDateFormat(validDateFormat);
            try {
                createdAt = simpleDateFormat.parse(date);
                break;
            } catch (ParseException e) {
                createdAt = null;
            }
        }
        if (createdAt == null) {
            throw new ParseException("Cannot parse date", 0);
        }
        return createdAt;
    }
}
