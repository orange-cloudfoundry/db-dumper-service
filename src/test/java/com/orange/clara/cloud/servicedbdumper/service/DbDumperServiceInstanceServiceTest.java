package com.orange.clara.cloud.servicedbdumper.service;

import com.google.common.collect.Maps;
import com.orange.clara.cloud.servicedbdumper.dbdumper.DatabaseRefManager;
import com.orange.clara.cloud.servicedbdumper.exception.DatabaseExtractionException;
import com.orange.clara.cloud.servicedbdumper.exception.ServiceKeyException;
import com.orange.clara.cloud.servicedbdumper.model.*;
import com.orange.clara.cloud.servicedbdumper.repo.*;
import com.orange.clara.cloud.servicedbdumper.task.job.JobFactory;
import org.cloudfoundry.community.servicebroker.exception.ServiceBrokerException;
import org.cloudfoundry.community.servicebroker.exception.ServiceInstanceDoesNotExistException;
import org.cloudfoundry.community.servicebroker.exception.ServiceInstanceExistsException;
import org.cloudfoundry.community.servicebroker.exception.ServiceInstanceUpdateNotSupportedException;
import org.cloudfoundry.community.servicebroker.model.*;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.net.URI;
import java.util.Arrays;
import java.util.Map;

import static org.fest.assertions.Assertions.assertThat;
import static org.fest.assertions.Fail.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.notNull;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;

/**
 * Copyright (C) 2016 Arthur Halet
 * <p>
 * This software is distributed under the terms and conditions of the 'MIT'
 * license which can be found in the file 'LICENSE' in this package distribution
 * or at 'http://opensource.org/licenses/MIT'.
 * <p>
 * Author: Arthur Halet
 * Date: 18/03/2016
 */
public class DbDumperServiceInstanceServiceTest {
    private final static String serviceDefinitionId = "service-definition-1";
    private final static String serviceId = "service-1";
    private final static String planId = "plan-1";
    private final static String orgId = "org-1";
    private final static String spaceId = "space-1";
    private final static String targetDatabase = "mysql://foo:bar@mysql.com:3306/mydb";
    private final static DbDumperPlan dbDumperPlan = new DbDumperPlan();
    private final static Map<String, Object> params = Maps.newHashMap();
    private final static CreateServiceInstanceRequest createRequest = new CreateServiceInstanceRequest(serviceDefinitionId, planId, orgId, spaceId, true, params).withServiceInstanceId(serviceId);
    private final static UpdateServiceInstanceRequest updateRequest = new UpdateServiceInstanceRequest(planId, true, params).withInstanceId(serviceId);
    private final static DeleteServiceInstanceRequest deleteRequest = new DeleteServiceInstanceRequest(serviceId, serviceId, planId, false);
    @InjectMocks
    DbDumperServiceInstanceService instanceService;
    @Mock
    DatabaseRefManager databaseRefManager;
    @Mock
    DbDumperServiceInstanceRepo repository;
    @Mock
    DbDumperServiceInstanceBindingRepo serviceInstanceBindingRepo;
    @Mock
    JobFactory jobFactory;
    @Mock
    JobRepo jobRepo;
    @Mock
    DatabaseRefRepo databaseRefRepo;
    @Mock
    DbDumperPlanRepo dbDumperPlanRepo;
    private DbDumperServiceInstance dbDumperServiceInstance;
    private DatabaseRef databaseRef;

    @Before
    public void init() throws DatabaseExtractionException, ServiceKeyException {
        initMocks(this);
        dbDumperServiceInstance = new DbDumperServiceInstance();
        params.clear();
        databaseRef = new DatabaseRef(serviceId, URI.create(targetDatabase));
        dbDumperServiceInstance.setDatabaseRef(databaseRef);
        instanceService.appUri = "http://dashboard.com";
        when(dbDumperPlanRepo.findOne(anyString())).thenReturn(dbDumperPlan);
        when(databaseRefManager.getDatabaseRef(anyString(), any(), any(), any())).thenReturn(databaseRef);
        when(databaseRefManager.updateDatabaseRef(databaseRef)).thenReturn(databaseRef);
    }

    @Test
    public void when_creating_dump_from_non_existing_service_which_already_exist_it_should_raise_an_exception() {
        when(repository.findOne(anyString())).thenReturn(dbDumperServiceInstance);
        try {
            instanceService.createServiceInstance(createRequest);
            fail("Should throw an ServiceInstanceExistsException");
        } catch (Exception e) {
            assertThat(e).isInstanceOf(ServiceInstanceExistsException.class);
        }
    }

    @Test
    public void when_creating_dump_from_non_existing_service_with_service_plan_non_existing_it_should_raise_an_exception() {
        when(repository.findOne(anyString())).thenReturn(null);
        when(dbDumperPlanRepo.findOne(anyString())).thenReturn(null);
        try {
            instanceService.createServiceInstance(createRequest);
            fail("Should throw an ServiceBrokerException");
        } catch (Exception e) {
            assertThat(e).isInstanceOf(ServiceBrokerException.class);
        }
    }

    @Test
    public void when_creating_dump_from_non_existing_service_with_missing_target_parameter_it_should_raise_an_exception() {
        when(repository.findOne(anyString())).thenReturn(null);
        try {
            instanceService.createServiceInstance(createRequest);
            fail("Should throw an ServiceBrokerException");
        } catch (Exception e) {
            assertThat(e).isInstanceOf(ServiceBrokerException.class);
        }
    }

    @Test
    public void when_creating_dump_from_non_existing_service_with_target_parameter_it_should_return_a_service_instance() throws ServiceBrokerException, ServiceInstanceExistsException {
        when(repository.findOne(anyString())).thenReturn(null);
        params.put("db", targetDatabase);
        ServiceInstance serviceInstance = instanceService.createServiceInstance(createRequest);
        assertServiceInstanceCreateRequest(serviceInstance);
    }

    @Test
    public void when_creating_dump_from_non_existing_service_with_target_parameter_and_metadata_it_should_return_a_service_instance() throws ServiceBrokerException, ServiceInstanceExistsException {
        when(repository.findOne(anyString())).thenReturn(null);
        params.put("db", targetDatabase);
        Map<String, Object> metadataParameters = Maps.newHashMap();
        metadataParameters.put("tags", Arrays.asList("mytags"));
        params.put("metadata", metadataParameters);

        ServiceInstance serviceInstance = instanceService.createServiceInstance(createRequest);
        assertServiceInstanceCreateRequest(serviceInstance);
        verify(jobFactory, times(1)).createJobCreateDump((DbDumperServiceInstance) notNull(), (Metadata) notNull());
    }

    @Test
    public void when_creating_dump_from_existing_service_instance_with_no_action_parameter_it_should_raise_an_exception() {
        when(repository.findOne(anyString())).thenReturn(dbDumperServiceInstance);
        try {
            instanceService.updateServiceInstance(updateRequest);
            fail("Should throw an ServiceBrokerException");
        } catch (Exception e) {
            assertThat(e).isInstanceOf(ServiceBrokerException.class);
        }
    }

    @Test
    public void when_restore_dump_with_no_target_parameter_it_should_raise_an_exception() {
        when(repository.findOne(anyString())).thenReturn(dbDumperServiceInstance);
        params.put("action", "restore");
        try {
            instanceService.updateServiceInstance(updateRequest);
            fail("Should throw an ServiceBrokerException");
        } catch (Exception e) {
            assertThat(e).isInstanceOf(ServiceBrokerException.class);
        }
    }

    @Test
    public void when_creating_dump_from_existing_service_instance_which_not_exist_it_should_raise_an_exception() {
        when(repository.findOne(anyString())).thenReturn(null);
        try {
            instanceService.updateServiceInstance(updateRequest);
            fail("Should throw an ServiceInstanceDoesNotExistException");
        } catch (Exception e) {
            assertThat(e).isInstanceOf(ServiceInstanceDoesNotExistException.class);
        }
    }

    @Test
    public void when_creating_dump_from_existing_service_instance_it_should_return_service_instance() throws ServiceInstanceDoesNotExistException, ServiceInstanceUpdateNotSupportedException, ServiceBrokerException {
        when(repository.findOne(anyString())).thenReturn(dbDumperServiceInstance);
        params.put("action", "dump");
        ServiceInstance serviceInstance = instanceService.updateServiceInstance(updateRequest);
        assertServiceInstanceUpdateRequest(serviceInstance);
    }


    @Test
    public void when_creating_dump_from_existing_service_instance_and_metadata_it_should_return_service_instance() throws ServiceInstanceDoesNotExistException, ServiceInstanceUpdateNotSupportedException, ServiceBrokerException {
        when(repository.findOne(anyString())).thenReturn(dbDumperServiceInstance);
        params.put("action", "dump");

        Map<String, Object> metadataParameters = Maps.newHashMap();
        metadataParameters.put("tags", Arrays.asList("mytags"));
        params.put("metadata", metadataParameters);

        ServiceInstance serviceInstance = instanceService.updateServiceInstance(updateRequest);
        assertServiceInstanceUpdateRequest(serviceInstance);

        verify(jobFactory, times(1)).createJobCreateDump((DbDumperServiceInstance) notNull(), (Metadata) notNull());
    }

    @Test
    public void when_restore_dump_from_existing_service_instance_it_should_return_service_instance() throws ServiceInstanceDoesNotExistException, ServiceInstanceUpdateNotSupportedException, ServiceBrokerException {
        when(repository.findOne(anyString())).thenReturn(dbDumperServiceInstance);
        params.put("action", "restore");
        params.put("db", targetDatabase);
        ServiceInstance serviceInstance = instanceService.updateServiceInstance(updateRequest);
        assertServiceInstanceUpdateRequest(serviceInstance);
    }

    @Test
    public void when_deleting_service_from_existing_service_instance_which_not_exist_it_should_alway_return_service_instance() throws ServiceBrokerException {
        when(repository.findOne(anyString())).thenReturn(null);
        ServiceInstance serviceInstance = instanceService.deleteServiceInstance(deleteRequest);
        this.assertServiceInstanceDeleteRequest(serviceInstance, true);
    }

    @Test
    public void when_deleting_service_should_return_service_instance() throws ServiceBrokerException {
        when(repository.findOne(anyString())).thenReturn(dbDumperServiceInstance);
        ServiceInstance serviceInstance = instanceService.deleteServiceInstance(deleteRequest);
        this.assertServiceInstanceDeleteRequest(serviceInstance, false);
    }

    @Test
    public void when_getting_service_instance_when_service_instance_not_exist_it_should_return_null() {
        when(repository.findOne(anyString())).thenReturn(null);
        ServiceInstance serviceInstance = instanceService.getServiceInstance(serviceId);
        assertThat(serviceInstance).isNull();
    }

    @Test
    public void when_getting_service_instance_and_no_last_job_it_should_return_service_instance_with_in_progress_last_operation() {
        when(repository.findOne(anyString())).thenReturn(dbDumperServiceInstance);
        ServiceInstance serviceInstance = instanceService.getServiceInstance(serviceId);
        this.assertServiceInstanceGetRequest(serviceInstance, false, null);
    }

    @Test
    public void when_getting_service_instance_and_last_job_is_errored_it_should_return_service_instance_with_failed_last_operation() {
        when(repository.findOne(anyString())).thenReturn(dbDumperServiceInstance);
        Job job = new Job();
        job.setJobEvent(JobEvent.ERRORED);
        job.setErrorMessage("Error");
        job.setJobType(JobType.CREATE_DUMP);
        when(jobRepo.findFirstByDbDumperServiceInstanceOrderByUpdatedAtDesc(dbDumperServiceInstance)).thenReturn(job);
        ServiceInstance serviceInstance = instanceService.getServiceInstance(serviceId);
        this.assertServiceInstanceGetRequest(serviceInstance, true, "failed");
    }

    @Test
    public void when_getting_service_instance_and_last_job_is_started_it_should_return_service_instance_with_in_progress_last_operation() {
        when(repository.findOne(anyString())).thenReturn(dbDumperServiceInstance);
        Job job = new Job();
        job.setJobEvent(JobEvent.START);
        job.setErrorMessage("start");
        job.setJobType(JobType.CREATE_DUMP);
        when(jobRepo.findFirstByDbDumperServiceInstanceOrderByUpdatedAtDesc(dbDumperServiceInstance)).thenReturn(job);
        ServiceInstance serviceInstance = instanceService.getServiceInstance(serviceId);
        this.assertServiceInstanceGetRequest(serviceInstance, true, "in progress");
    }

    @Test
    public void when_getting_service_instance_and_last_job_is_finished_it_should_return_service_instance_with_succeeded_last_operation() {
        when(repository.findOne(anyString())).thenReturn(dbDumperServiceInstance);
        Job job = new Job();
        job.setJobEvent(JobEvent.FINISHED);
        job.setErrorMessage("finished");
        job.setJobType(JobType.CREATE_DUMP);
        when(jobRepo.findFirstByDbDumperServiceInstanceOrderByUpdatedAtDesc(dbDumperServiceInstance)).thenReturn(job);
        ServiceInstance serviceInstance = instanceService.getServiceInstance(serviceId);
        this.assertServiceInstanceGetRequest(serviceInstance, true, "succeeded");
    }

    private void assertServiceInstanceCreateRequest(ServiceInstance serviceInstance) {
        assertThat(serviceInstance).isNotNull();
        assertThat(serviceInstance.getDashboardUrl()).isNotEmpty();
        assertThat(serviceInstance.isAsync()).isTrue();
        assertThat(serviceInstance.getOrganizationGuid()).isEqualTo(orgId);
        assertThat(serviceInstance.getPlanId()).isEqualTo(planId);
        assertThat(serviceInstance.getServiceDefinitionId()).isEqualTo(serviceDefinitionId);
        assertThat(serviceInstance.getServiceInstanceId()).isEqualTo(serviceId);
        assertThat(serviceInstance.getSpaceGuid()).isEqualTo(spaceId);
    }

    private void assertServiceInstanceUpdateRequest(ServiceInstance serviceInstance) {
        assertThat(serviceInstance).isNotNull();
        assertThat(serviceInstance.getDashboardUrl()).isNotEmpty();
        assertThat(serviceInstance.isAsync()).isTrue();
        assertThat(serviceInstance.getPlanId()).isEqualTo(planId);
        assertThat(serviceInstance.getServiceInstanceId()).isEqualTo(serviceId);
    }

    private void assertServiceInstanceDeleteRequest(ServiceInstance serviceInstance, boolean emptyDashboard) {
        assertThat(serviceInstance).isNotNull();
        if (emptyDashboard) {
            assertThat(serviceInstance.getDashboardUrl()).isNull();
        } else {
            assertThat(serviceInstance.getDashboardUrl()).isNotNull();
            assertThat(serviceInstance.getDashboardUrl()).isNotEmpty();
        }

        assertThat(serviceInstance.isAsync()).isFalse();
        assertThat(serviceInstance.getPlanId()).isEqualTo(planId);
        assertThat(serviceInstance.getServiceInstanceId()).isEqualTo(serviceId);
    }

    private void assertServiceInstanceGetRequest(ServiceInstance serviceInstance, boolean withLastOperation, String operationState) {
        assertThat(serviceInstance).isNotNull();
        assertThat(serviceInstance.getServiceInstanceId()).isEqualTo(serviceId);
        assertThat(serviceInstance.getDashboardUrl()).isNotNull();
        assertThat(serviceInstance.getDashboardUrl()).isNotEmpty();
        if (!withLastOperation) {
            assertThat(serviceInstance.getServiceInstanceLastOperation()).isNotNull();
            assertThat(serviceInstance.getServiceInstanceLastOperation().getState()).isEqualTo("in progress");
            assertThat(serviceInstance.isAsync()).isFalse();
            return;
        }
        assertThat(serviceInstance.getServiceInstanceLastOperation()).isNotNull();
        assertThat(serviceInstance.isAsync()).isTrue();

        ServiceInstanceLastOperation serviceInstanceLastOperation = serviceInstance.getServiceInstanceLastOperation();
        assertThat(serviceInstanceLastOperation.getState()).isEqualTo(operationState);
        assertThat(serviceInstanceLastOperation.getDescription()).isNotEmpty();
    }
}