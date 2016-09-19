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
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static org.fest.assertions.Assertions.assertThat;
import static org.fest.assertions.Fail.fail;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.notNull;
import static org.mockito.Mockito.when;
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
public class DbDumperServiceInstanceBindingServiceTest {

    private final static String serviceDefinitionId = "service-definition-1";
    private final static String serviceId = "service-1";
    private final static String bindingId = "binding-1";
    private final static String planId = "plan-1";
    private final static String appGuid = "01-02";
    private final static DbDumperServiceInstance dbDumperServiceInstance = new DbDumperServiceInstance(serviceId, planId, "org-1", "space-1", "http://dashboard.com", null);
    private final static DeleteServiceInstanceBindingRequest deleteRequest = new DeleteServiceInstanceBindingRequest(bindingId, null, serviceId, planId);
    private final static DbDumperServiceInstanceBinding dbDumperServiceInstanceBinding = new DbDumperServiceInstanceBinding(bindingId, dbDumperServiceInstance, appGuid);
    private final static DatabaseRef databaseRef = new DatabaseRef();

    @InjectMocks
    DbDumperServiceInstanceBindingService instanceBindingService;
    String dateFormat = "dd-MM-yyyy HH:mm";
    @Mock
    DbDumperServiceInstanceBindingRepo repositoryInstanceBinding;
    @Mock
    DbDumperServiceInstanceRepo repositoryInstance;
    @Mock
    Credentials credentials;
    private Map<String, Object> parameters;
    private CreateServiceInstanceBindingRequest createRequest;
    private DbDumperCredential dbDumperCredential1;
    private DbDumperCredential dbDumperCredential2;
    private DbDumperCredential dbDumperCredential3;
    private List<DbDumperCredential> dbDumperCredentials;

    @Before
    public void init() {
        initMocks(this);
        dbDumperServiceInstance.setDatabaseRef(databaseRef);
        parameters = Maps.newHashMap();
        createRequest = new CreateServiceInstanceBindingRequest(serviceDefinitionId, planId, appGuid, parameters)
                .withBindingId(bindingId)
                .withServiceInstanceId(serviceId);
        this.dbDumperCredential1 = this.forgeDbDumperCredential(1, false);
        this.dbDumperCredential2 = this.forgeDbDumperCredential(2, false);
        this.dbDumperCredential3 = this.forgeDbDumperCredential(3, true);
        dbDumperCredentials = Arrays.asList(dbDumperCredential1, dbDumperCredential2, dbDumperCredential3);
        instanceBindingService.dateFormat = this.dateFormat;

        when(repositoryInstance.findOne(anyString())).thenReturn(dbDumperServiceInstance);
        when(credentials.getDumpsCredentials((DbDumperServiceInstance) notNull())).thenReturn(dbDumperCredentials);
    }

    @Test
    public void when_creating_service_instance_binding_which_already_exist_it_should_raise_an_exception() {
        when(repositoryInstanceBinding.findOne(anyString())).thenReturn(dbDumperServiceInstanceBinding);
        try {
            instanceBindingService.createServiceInstanceBinding(createRequest);
            fail("Should throw an ServiceInstanceBindingExistsException");
        } catch (Exception e) {
            assertThat(e).isInstanceOf(ServiceInstanceBindingExistsException.class);
        }
    }

    @Test
    public void when_creating_service_instance_binding_with_service_id_non_existing_it_should_raise_an_exception() {
        when(repositoryInstanceBinding.findOne(anyString())).thenReturn(null);
        when(repositoryInstance.findOne(anyString())).thenReturn(null);
        try {
            instanceBindingService.createServiceInstanceBinding(createRequest);
            fail("Should throw an ServiceBrokerException");
        } catch (Exception e) {
            assertThat(e).isInstanceOf(ServiceBrokerException.class);
        }
    }

    @Test
    public void when_deleting_service_instance_binding_which_non_existing_service_instance_it_should_raise_an_exception() {
        when(repositoryInstanceBinding.findOne(anyString())).thenReturn(null);
        try {
            instanceBindingService.deleteServiceInstanceBinding(deleteRequest);
            fail("Should throw an ServiceBrokerException");
        } catch (Exception e) {
            assertThat(e).isInstanceOf(ServiceBrokerException.class);
        }
    }


    @Test
    public void when_deleting_service_instance_binding_with_service_id_existing_it_should_give_a_correct_service_instance_binding() throws ServiceBrokerException {
        when(repositoryInstanceBinding.findOne(anyString())).thenReturn(dbDumperServiceInstanceBinding);
        ServiceInstanceBinding instanceBinding = this.instanceBindingService.deleteServiceInstanceBinding(deleteRequest);
        assertThat(instanceBinding).isNotNull();
        assertThat(instanceBinding.getAppGuid()).isEqualTo(appGuid);
        assertThat(instanceBinding.getId()).isEqualTo(bindingId);
        assertThat(instanceBinding.getCredentials()).hasSize(0);
    }

    @Test
    public void when_creating_service_instance_binding_with_service_id_existing_it_should_give_a_correct_service_instance_binding() throws ServiceInstanceBindingExistsException, ServiceBrokerException {
        when(repositoryInstanceBinding.findOne(anyString())).thenReturn(null);
        ServiceInstanceBinding instanceBinding = this.instanceBindingService.createServiceInstanceBinding(createRequest);
        assertThat(instanceBinding).isNotNull();
        assertThat(instanceBinding.getAppGuid()).isEqualTo(appGuid);
        assertThat(instanceBinding.getId()).isEqualTo(bindingId);
        assertCredentials(instanceBinding, this.dbDumperCredentials);
    }

    @Test
    public void when_creating_service_instance_binding_with_service_id_existing_and_with_parameter_to_see_all_dump_to_false_it_should_give_a_correct_service_instance_binding() throws ServiceInstanceBindingExistsException, ServiceBrokerException {
        when(repositoryInstanceBinding.findOne(anyString())).thenReturn(null);
        parameters.put(DbDumperServiceInstanceBindingService.SEE_ALL_DUMPS_KEY, false);
        ServiceInstanceBinding instanceBinding = this.instanceBindingService.createServiceInstanceBinding(createRequest);
        assertThat(instanceBinding).isNotNull();
        assertThat(instanceBinding.getAppGuid()).isEqualTo(appGuid);
        assertThat(instanceBinding.getId()).isEqualTo(bindingId);
        assertCredentials(instanceBinding, this.dbDumperCredentials);
    }

    @Test
    public void when_creating_service_instance_binding_with_service_id_existing_and_user_ask_to_see_all_dumps_it_should_give_a_correct_service_instance_binding() throws ServiceInstanceBindingExistsException, ServiceBrokerException {
        when(repositoryInstanceBinding.findOne(anyString())).thenReturn(null);

        DbDumperCredential dbDumperCredential = this.forgeDbDumperCredential(dbDumperCredentials.size() + 1, true);
        List<DbDumperCredential> dumperCredentials = Arrays.asList(dbDumperCredential1, dbDumperCredential2, dbDumperCredential3, dbDumperCredential);
        when(credentials.getDumpsCredentials((DatabaseRef) notNull())).thenReturn(dumperCredentials);

        parameters.put(DbDumperServiceInstanceBindingService.SEE_ALL_DUMPS_KEY, true);

        ServiceInstanceBinding instanceBinding = this.instanceBindingService.createServiceInstanceBinding(createRequest);
        assertThat(instanceBinding).isNotNull();
        assertThat(instanceBinding.getAppGuid()).isEqualTo(appGuid);
        assertThat(instanceBinding.getId()).isEqualTo(bindingId);
        assertCredentials(instanceBinding, dumperCredentials);
    }

    @Test
    public void when_creating_service_instance_binding_with_service_id_existing_and_user_find_by_tags_it_should_give_a_correct_service_instance_binding() throws ServiceInstanceBindingExistsException, ServiceBrokerException {
        when(repositoryInstanceBinding.findOne(anyString())).thenReturn(null);
        String tag = "mytag";
        DbDumperCredential dbDumperCredential = this.forgeDbDumperCredential(dbDumperCredentials.size() + 1, true);
        dbDumperCredential.setTags(Arrays.asList(tag));

        List<DbDumperCredential> dumperCredentials = Arrays.asList(dbDumperCredential1, dbDumperCredential2, dbDumperCredential3, dbDumperCredential);
        when(credentials.getDumpsCredentials((DbDumperServiceInstance) notNull())).thenReturn(dumperCredentials);

        parameters.put(DbDumperServiceInstanceBindingService.FIND_BY_TAGS_KEY, Arrays.asList(tag));

        ServiceInstanceBinding instanceBinding = this.instanceBindingService.createServiceInstanceBinding(createRequest);
        assertThat(instanceBinding).isNotNull();
        assertThat(instanceBinding.getAppGuid()).isEqualTo(appGuid);
        assertThat(instanceBinding.getId()).isEqualTo(bindingId);
        assertCredentials(instanceBinding, Arrays.asList(dbDumperCredential));
    }

    public void assertCredentials(ServiceInstanceBinding serviceInstanceBinding, List<DbDumperCredential> dbDumperCredentials) {
        Map<String, Object> credentials = serviceInstanceBinding.getCredentials();
        List<Map<String, Object>> dumpFiles = (List<Map<String, Object>>) credentials.get("dumps");
        assertThat(dumpFiles).hasSize(dbDumperCredentials.size());
        SimpleDateFormat dateFormater = new SimpleDateFormat(this.dateFormat);
        for (int i = 0; i < dumpFiles.size(); i++) {
            Map<String, Object> dumpFile = dumpFiles.get(i);
            DbDumperCredential dbDumperCredential = dbDumperCredentials.get(i);
            assertThat(dumpFile.get("size")).isEqualTo(dbDumperCredential.getSize());
            assertThat(dumpFile.get("download_url")).isEqualTo(dbDumperCredential.getDownloadUrl());
            assertThat(dumpFile.get("show_url")).isEqualTo(dbDumperCredential.getShowUrl());
            assertThat(dumpFile.get("filename")).isEqualTo(dbDumperCredential.getFilename());
            assertThat(dumpFile.get("created_at")).isEqualTo(dateFormater.format(dbDumperCredential.getCreatedAt()));
            assertThat(dumpFile.get("dump_id")).isEqualTo(dbDumperCredential.getId());
            assertThat(dumpFile.get("deleted")).isEqualTo(dbDumperCredential.getDeleted());
            assertThat(dumpFile.get("tags")).isEqualTo(dbDumperCredential.getTags());
        }

    }

    private DbDumperCredential forgeDbDumperCredential(int id, boolean deleted) {
        Date date = new Date();
        LocalDateTime localDateTime = LocalDateTime.from(date.toInstant().atZone(ZoneId.systemDefault())).plusDays(id);
        Instant instant = localDateTime.atZone(ZoneId.systemDefault()).toInstant();
        return new DbDumperCredential(
                id,
                Date.from(instant),
                "http://download.com/" + id,
                "http://show.com/" + id,
                "file-" + id + ".txt",
                10L * id,
                deleted
        );
    }
}