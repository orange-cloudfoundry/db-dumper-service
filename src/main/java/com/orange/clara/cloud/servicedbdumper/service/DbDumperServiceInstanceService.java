package com.orange.clara.cloud.servicedbdumper.service;

import com.orange.clara.cloud.servicedbdumper.config.Routes;
import com.orange.clara.cloud.servicedbdumper.dbdumper.DatabaseRefManager;
import com.orange.clara.cloud.servicedbdumper.exception.DatabaseExtractionException;
import com.orange.clara.cloud.servicedbdumper.exception.RestoreCannotFindFileException;
import com.orange.clara.cloud.servicedbdumper.exception.RestoreException;
import com.orange.clara.cloud.servicedbdumper.exception.ServiceKeyException;
import com.orange.clara.cloud.servicedbdumper.model.*;
import com.orange.clara.cloud.servicedbdumper.repo.DbDumperPlanRepo;
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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static com.orange.clara.cloud.servicedbdumper.helper.ParameterParser.getParameter;
import static com.orange.clara.cloud.servicedbdumper.helper.ParameterParser.getParameterAsString;

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
    public final static String METADATA_PARAMETER = "metadata";
    public final static String TAGS_SUB_PARAMETER = "tags";


    private final static String DASHBOARD_ROUTE = Routes.MANAGE_ROOT + Routes.MANAGE_LIST + "/";
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
    private DbDumperPlanRepo dbDumperPlanRepo;

    @Override
    public ServiceInstance createServiceInstance(CreateServiceInstanceRequest request) throws ServiceInstanceExistsException, ServiceBrokerException {
        DbDumperServiceInstance dbDumperServiceInstance = repository.findOne(request.getServiceInstanceId());
        if (dbDumperServiceInstance != null && !dbDumperServiceInstance.isDeleted()) {
            throw new ServiceInstanceExistsException(new ServiceInstance(request));
        }
        DbDumperPlan dbDumperPlan = dbDumperPlanRepo.findOne(request.getPlanId());
        if (dbDumperPlan == null) {
            throw new ServiceBrokerException("Plan '" + request.getPlanId() + "' is not available.");
        }
        if (dbDumperServiceInstance != null && dbDumperServiceInstance.isDeleted()) {
            dbDumperServiceInstance.setDeleted(false);
        }
        if (dbDumperServiceInstance == null) {
            dbDumperServiceInstance = new DbDumperServiceInstance(
                    request.getServiceInstanceId(),
                    request.getPlanId(),
                    request.getOrganizationGuid(),
                    request.getSpaceGuid(),
                    appUri + DASHBOARD_ROUTE,
                    dbDumperPlan);
        }

        this.createDump(request.getParameters(), dbDumperServiceInstance);
        return new ServiceInstance(request).withDashboardUrl(appUri + DASHBOARD_ROUTE + dbDumperServiceInstance.getServiceInstanceId()).withAsync(true);
    }

    @Override
    public ServiceInstance getServiceInstance(String serviceInstanceId) {
        DbDumperServiceInstance instance = repository.findOne(serviceInstanceId);
        if (instance == null || instance.isDeleted()) {
            return null;
        }
        ServiceInstanceLastOperation serviceInstanceLastOperation = null;

        Job lastJob = this.jobRepo.findFirstByDbDumperServiceInstanceOrderByUpdatedAtDesc(instance);
        ServiceInstance serviceInstance = new ServiceInstance(new CreateServiceInstanceRequest().withServiceInstanceId(serviceInstanceId))
                .withDashboardUrl(appUri + DASHBOARD_ROUTE + instance.getDatabaseRef().getName());
        if (lastJob == null) {
            serviceInstanceLastOperation = new ServiceInstanceLastOperation("Error: job doesn't exists", OperationState.FAILED);
            return serviceInstance.withAsync(true).withLastOperation(serviceInstanceLastOperation);
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
        if (dbDumperServiceInstance == null || dbDumperServiceInstance.isDeleted()) {
            logger.warn("The service instance '" + request.getServiceInstanceId() + "' doesn't exist. Defaulting to say to cloud controller that instance is deleted.");
            return new ServiceInstance(request);
        }
        this.jobRepo.deleteByDbDumperServiceInstance(dbDumperServiceInstance);


        this.serviceInstanceBindingRepo.deleteByDbDumperServiceInstance(dbDumperServiceInstance);

        dbDumperServiceInstance.setDeleted(true);
        repository.save(dbDumperServiceInstance);

        this.jobFactory.createJobDeleteDbDumperServiceInstance(dbDumperServiceInstance);
        return new ServiceInstance(request).withDashboardUrl(appUri + DASHBOARD_ROUTE + dbDumperServiceInstance.getServiceInstanceId()).withAsync(false);
    }

    @Override
    public ServiceInstance updateServiceInstance(UpdateServiceInstanceRequest request) throws ServiceInstanceUpdateNotSupportedException, ServiceBrokerException, ServiceInstanceDoesNotExistException {
        DbDumperServiceInstance instance = repository.findOne(request.getServiceInstanceId());
        ServiceInstance serviceInstance = new ServiceInstance(request);
        if (instance == null || instance.isDeleted()) {
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
            this.createDumpFromdbDumperServiceInstance(parameters, instance);
        } else if (action.equals(UpdateAction.RESTORE)) {
            try {
                this.restoreDump(request.getParameters(), instance);
            } catch (RestoreCannotFindFileException e) {
                throw new ServiceBrokerException(e.getMessage());
            } catch (RestoreException e) {
                throw new ServiceBrokerException("An error occurred during restore: " + e.getMessage(), e);
            }
        }
        serviceInstance.withDashboardUrl(appUri + DASHBOARD_ROUTE + instance.getServiceInstanceId()).withAsync(true);
        return serviceInstance;
    }

    private void createDumpFromdbDumperServiceInstance(Map<String, Object> parameters, DbDumperServiceInstance dbDumperServiceInstance) throws ServiceInstanceDoesNotExistException, ServiceBrokerException {
        if (dbDumperServiceInstance.getDatabaseRef() == null) {
            throw new ServiceInstanceDoesNotExistException("There is no database set for this instance");
        }
        try {
            dbDumperServiceInstance.setDatabaseRef(this.databaseRefManager.updateDatabaseRef(dbDumperServiceInstance.getDatabaseRef()));
        } catch (ServiceKeyException | DatabaseExtractionException e) {
            throw new ServiceBrokerException("An error occurred during dump: " + e.getMessage(), e);
        }
        this.createJobToCreateDump(parameters, dbDumperServiceInstance);
    }

    private void createDump(Map<String, Object> parameters, DbDumperServiceInstance dbDumperServiceInstance) throws ServiceBrokerException {
        String srcUrl = getParameterAsString(parameters, SRC_URL_PARAMETER, null);
        if (srcUrl == null) {
            srcUrl = getParameterAsString(parameters, NEW_SRC_URL_PARAMETER);
        }
        DatabaseRef databaseRef = this.getDatabaseRefFromParams(parameters, srcUrl);
        dbDumperServiceInstance.setDatabaseRef(databaseRef);
        repository.save(dbDumperServiceInstance);
        this.createJobToCreateDump(parameters, dbDumperServiceInstance);
    }

    private void createJobToCreateDump(Map<String, Object> parameters, DbDumperServiceInstance dbDumperServiceInstance) {
        Map<String, Object> metadataParameters = (Map<String, Object>) getParameter(parameters, METADATA_PARAMETER, null, Map.class);
        if (metadataParameters == null) {
            this.jobFactory.createJobCreateDump(dbDumperServiceInstance);
            return;
        }
        List<String> tags = (List<String>) getParameter(metadataParameters, TAGS_SUB_PARAMETER, null, List.class);
        Metadata metadata = new Metadata();
        metadata.setTags(tags);
        this.jobFactory.createJobCreateDump(dbDumperServiceInstance, metadata);
    }

    private DatabaseRef getDatabaseRefFromParams(Map<String, Object> parameters, String dbUrlOrService) throws ServiceBrokerException {
        String token = getParameterAsString(parameters, CF_USER_TOKEN_PARAMETER, null);
        String org = getParameterAsString(parameters, ORG_PARAMETER, null);
        String space = getParameterAsString(parameters, SPACE_PARAMETER, null);
        try {
            return this.databaseRefManager.getDatabaseRef(dbUrlOrService, token, org, space);
        } catch (ServiceKeyException | DatabaseExtractionException e) {
            throw new ServiceBrokerException("Error when getting database: " + e.getMessage(), e);
        }
    }

    private void restoreDump(Map<String, Object> parameters, DbDumperServiceInstance dbDumperServiceInstance) throws ServiceBrokerException, RestoreException {
        String targetUrl = getParameterAsString(parameters, TARGET_URL_PARAMETER, null);
        if (targetUrl == null) {
            targetUrl = getParameterAsString(parameters, NEW_TARGET_URL_PARAMETER);
        }
        String createdAtString = getParameterAsString(parameters, CREATED_AT_PARAMETER, null);
        DatabaseRef databaseRefTarget = this.getDatabaseRefFromParams(parameters, targetUrl);
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
