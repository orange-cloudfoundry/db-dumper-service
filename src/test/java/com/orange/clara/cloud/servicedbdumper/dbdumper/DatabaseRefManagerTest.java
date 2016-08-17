package com.orange.clara.cloud.servicedbdumper.dbdumper;

import com.google.common.collect.Maps;
import com.orange.clara.cloud.servicedbdumper.exception.DatabaseExtractionException;
import com.orange.clara.cloud.servicedbdumper.exception.ServiceKeyException;
import com.orange.clara.cloud.servicedbdumper.fake.cloudservicekey.MockCloudServiceKey;
import com.orange.clara.cloud.servicedbdumper.fake.services.MockServiceKeyManager;
import com.orange.clara.cloud.servicedbdumper.model.DatabaseRef;
import com.orange.clara.cloud.servicedbdumper.model.DatabaseService;
import com.orange.clara.cloud.servicedbdumper.model.DatabaseType;
import com.orange.clara.cloud.servicedbdumper.repo.DatabaseRefRepo;
import com.orange.clara.cloud.servicedbdumper.repo.DatabaseServiceRepo;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;

import java.net.URI;
import java.util.Map;
import java.util.UUID;

import static org.fest.assertions.Assertions.assertThat;
import static org.fest.assertions.Fail.fail;
import static org.mockito.Matchers.notNull;
import static org.mockito.Mockito.reset;
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
 * Date: 29/02/2016
 */
public class DatabaseRefManagerTest {
    private final static String databaseName = "database";
    private final static DatabaseType databaseType = DatabaseType.MYSQL;
    private final static String user = "foo";
    private final static String password = "bar";
    private final static String host = "mydb.com";
    private final static String serviceName = "mysql-myservice";
    private final static String token = "mytoken";
    private final static String space = "space";
    private final static String org = "org";
    @InjectMocks
    DatabaseRefManager databaseRefManager;
    @Mock
    DatabaseRefRepo databaseRefRepo;
    @Mock
    DatabaseServiceRepo databaseServiceRepo;
    @Spy
    MockServiceKeyManager serviceKeyManager = new MockServiceKeyManager();


    @Before
    public void init() {
        reset();
        initMocks(this);
        when(databaseRefRepo.save((DatabaseRef) notNull())).thenReturn(null);
        when(databaseServiceRepo.save((DatabaseService) notNull())).thenReturn(null);
    }

    @Test
    public void pass_token_org_space_and_service_name_with_uri_credentials_give_appropriate_database_ref() throws DatabaseExtractionException, ServiceKeyException {
        when(databaseRefRepo.exists((String) notNull())).thenReturn(false);
        when(databaseServiceRepo.exists((String) notNull())).thenReturn(false);

        Map<String, Object> credentials = Maps.newHashMap();
        credentials.put("uri", this.generateUri());
        MockCloudServiceKey mockCloudServiceKey = new MockCloudServiceKey(credentials, serviceName);
        this.serviceKeyManager.setCloudServiceKey(mockCloudServiceKey);

        assertDatabaseRef(this.databaseRefManager.getDatabaseRef(serviceName, token, org, space), mockCloudServiceKey);
    }

    @Test
    public void pass_token_and_service_name_with_uri_credentials_give_appropriate_database_ref() throws DatabaseExtractionException, ServiceKeyException {
        when(databaseRefRepo.exists((String) notNull())).thenReturn(false);
        when(databaseServiceRepo.exists((String) notNull())).thenReturn(false);

        Map<String, Object> credentials = Maps.newHashMap();
        credentials.put("uri", this.generateUri());
        MockCloudServiceKey mockCloudServiceKey = new MockCloudServiceKey(credentials, serviceName);
        this.serviceKeyManager.setCloudServiceKey(mockCloudServiceKey);
        assertDatabaseRef(this.databaseRefManager.getDatabaseRef(serviceName, token, null, null), mockCloudServiceKey);
    }

    @Test
    public void pass_database_uri_give_appropriate_database_ref() throws DatabaseExtractionException, ServiceKeyException {
        when(databaseRefRepo.exists((String) notNull())).thenReturn(false);
        when(databaseServiceRepo.exists((String) notNull())).thenReturn(false);
        String uri = this.generateUri();
        assertDatabaseRef(this.databaseRefManager.getDatabaseRef(uri, null, null, null), this.databaseRefManager.generateDatabaseRefName(uri));
    }

    @Test
    public void pass_database_malformed_uri_throw_an_error() throws DatabaseExtractionException, ServiceKeyException {
        when(databaseRefRepo.exists((String) notNull())).thenReturn(false);
        when(databaseServiceRepo.exists((String) notNull())).thenReturn(false);
        try {
            this.databaseRefManager.getDatabaseRef("sowrong://mydb.com", null, null, null);
            fail("A DatabaseExtractionException must be thrown");
        } catch (Exception e) {
            assertThat(e).isInstanceOf(DatabaseExtractionException.class);
        }
    }

    @Test
    public void update_database_ref_without_database_service_give_the_same_database_ref() throws DatabaseExtractionException, ServiceKeyException {
        DatabaseRef databaseRef = new DatabaseRef("mydb", URI.create(this.generateUri()));

        DatabaseRef updatedDatabaseRef = this.databaseRefManager.updateDatabaseRef(databaseRef);

        assertThat(updatedDatabaseRef.getDatabaseDumpFiles()).hasSize(0);
        assertThat(updatedDatabaseRef.getDatabaseService()).isNull();
        assertThat(updatedDatabaseRef.getName()).isEqualTo(databaseRef.getName());
        assertThat(updatedDatabaseRef.getType()).isEqualTo(databaseRef.getType());
        assertThat(updatedDatabaseRef.getUser()).isEqualTo(databaseRef.getUser());
        assertThat(updatedDatabaseRef.getPassword()).isEqualTo(databaseRef.getPassword());
        assertThat(updatedDatabaseRef.getDeleted()).isEqualTo(databaseRef.getDeleted());
        assertThat(updatedDatabaseRef.getHost()).isEqualTo(databaseRef.getHost());
        assertThat(updatedDatabaseRef.getDatabaseName()).isEqualTo(databaseRef.getDatabaseName());
    }

    @Test
    public void update_database_ref_with_database_service_give_a_database_ref_with_new_user_password() throws DatabaseExtractionException, ServiceKeyException {
        String newPassword = "mynewpassword";
        String newUser = "mynewuser";
        String newHost = "mynewhost";
        String newDatabaseName = "mynewname";

        Map<String, Object> credentials = Maps.newHashMap();
        credentials.put("password", newPassword);
        credentials.put("user", newUser);
        credentials.put("host", newHost);
        credentials.put("name", newDatabaseName);

        MockCloudServiceKey mockCloudServiceKey = new MockCloudServiceKey(credentials, serviceName);
        this.serviceKeyManager.setCloudServiceKey(mockCloudServiceKey);

        String dbName = mockCloudServiceKey.getService().getMeta().getGuid().toString();
        DatabaseRef databaseRef = new DatabaseRef(dbName, URI.create(this.generateUri()));
        DatabaseService databaseService = new DatabaseService(dbName, serviceName, org, space);
        databaseRef.setDatabaseService(databaseService);


        DatabaseRef updatedDatabaseRef = this.databaseRefManager.updateDatabaseRef(databaseRef);

        assertThat(updatedDatabaseRef.getDatabaseDumpFiles()).hasSize(0);
        assertThat(updatedDatabaseRef.getDatabaseService()).isNotNull();

        DatabaseService updatedDatabaseService = updatedDatabaseRef.getDatabaseService();

        assertThat(updatedDatabaseRef.getName()).isEqualTo(dbName);
        assertThat(updatedDatabaseRef.getType()).isEqualTo(databaseRef.getType());
        assertThat(updatedDatabaseRef.getUser()).isEqualTo(newUser);
        assertThat(updatedDatabaseRef.getPassword()).isEqualTo(newPassword);
        assertThat(updatedDatabaseRef.getDeleted()).isEqualTo(databaseRef.getDeleted());
        assertThat(updatedDatabaseRef.getHost()).isEqualTo(newHost);
        assertThat(updatedDatabaseRef.getDatabaseName()).isEqualTo(newDatabaseName);

        assertThat(updatedDatabaseService.getUuid()).isEqualTo(dbName);
        assertThat(updatedDatabaseService.getName()).isEqualTo(serviceName);
        assertThat(updatedDatabaseService.getSpace()).isEqualTo(space);
        assertThat(updatedDatabaseService.getOrg()).isEqualTo(org);
        assertThat(updatedDatabaseService.getServiceKeyGuid()).isEqualTo(mockCloudServiceKey.getMeta().getGuid().toString());
    }

    @Test
    public void pass_token_org_space_and_service_name_with_wrong_uri_credentials_throw_an_error() {
        when(databaseRefRepo.exists((String) notNull())).thenReturn(false);
        when(databaseServiceRepo.exists((String) notNull())).thenReturn(false);

        Map<String, Object> credentials = Maps.newHashMap();
        credentials.put("uri", "thisisawronguri$");
        MockCloudServiceKey mockCloudServiceKey = new MockCloudServiceKey(credentials, serviceName);
        this.serviceKeyManager.setCloudServiceKey(mockCloudServiceKey);

        try {
            this.databaseRefManager.getDatabaseRef(serviceName, token, org, space);
            fail("A DatabaseExtractionException must be thrown");
        } catch (Exception e) {
            assertThat(e).isInstanceOf(DatabaseExtractionException.class);
        }
    }

    @Test
    public void ensure_when_delete_service_key_it_doesnt_appear_in_database_service() throws DatabaseExtractionException {
        DatabaseRef databaseRef = new DatabaseRef(UUID.randomUUID().toString(), URI.create(this.generateUri()));
        DatabaseService databaseService = new DatabaseService(UUID.randomUUID().toString(), serviceName, org, space);
        databaseService.setServiceKeyGuid(UUID.randomUUID().toString());
        databaseRef.setDatabaseService(databaseService);

        this.databaseRefManager.deleteServiceKey(databaseRef);
        assertThat(databaseRef.getUser()).isEmpty();
        assertThat(databaseRef.getPassword()).isEmpty();
        assertThat(databaseRef.getDatabaseService().getServiceKeyGuid()).isNull();
    }

    @Test
    public void pass_token_org_space_and_service_name_with_service_name_not_contains_type_throw_an_error() {
        when(databaseRefRepo.exists((String) notNull())).thenReturn(false);
        when(databaseServiceRepo.exists((String) notNull())).thenReturn(false);

        Map<String, Object> credentials = Maps.newHashMap();
        credentials.put("password", password);
        credentials.put("user", user);
        credentials.put("host", host);
        credentials.put("name", databaseName);
        MockCloudServiceKey mockCloudServiceKey = new MockCloudServiceKey(credentials, "service");
        this.serviceKeyManager.setCloudServiceKey(mockCloudServiceKey);
        try {
            this.databaseRefManager.getDatabaseRef("service", token, org, space);
            fail("A DatabaseExtractionException must be thrown");
        } catch (Exception e) {
            assertThat(e).isInstanceOf(DatabaseExtractionException.class);
        }
    }

    @Test
    public void pass_token_org_space_and_service_name_without_token_throw_an_error() {
        try {
            this.databaseRefManager.getDatabaseRef(serviceName, null, org, space);
            fail("A ServiceKeyException must be thrown");
        } catch (Exception e) {
            assertThat(e).isInstanceOf(ServiceKeyException.class);
        }

    }

    @Test
    public void pass_token_org_space_and_service_name_with_inline_credentials_give_appropriate_database_ref() throws DatabaseExtractionException, ServiceKeyException {
        when(databaseRefRepo.exists((String) notNull())).thenReturn(false);
        when(databaseServiceRepo.exists((String) notNull())).thenReturn(false);

        Map<String, Object> credentials = Maps.newHashMap();
        credentials.put("password", password);
        credentials.put("user", user);
        credentials.put("host", host);
        credentials.put("name", databaseName);
        MockCloudServiceKey mockCloudServiceKey = new MockCloudServiceKey(credentials, serviceName);
        this.serviceKeyManager.setCloudServiceKey(mockCloudServiceKey);

        assertDatabaseRef(this.databaseRefManager.getDatabaseRef(serviceName, token, org, space), mockCloudServiceKey);

    }

    @Test
    public void ensure_when_bearer_is_in_token_its_sanitize() {
        String expectedToken = "mytoken";
        assertThat(this.databaseRefManager.sanitizeToken(expectedToken)).isEqualTo(expectedToken);
        assertThat(this.databaseRefManager.sanitizeToken("Bearer " + expectedToken)).isEqualTo(expectedToken);
        assertThat(this.databaseRefManager.sanitizeToken("bearer " + expectedToken)).isEqualTo(expectedToken);
        assertThat(this.databaseRefManager.sanitizeToken("bearer" + expectedToken)).isEqualTo(expectedToken);
    }

    private String generateUri() {
        return databaseType.toString().toLowerCase() + "://" + user + ":" + password + "@" + host + "/" + databaseName;
    }

    private void assertDatabaseRef(DatabaseRef databaseRef, String uuid) {
        assertThat(databaseRef).isNotNull();
        assertThat(databaseRef.getDatabaseDumpFiles()).hasSize(0);
        assertThat(databaseRef.getDeleted()).isFalse();
        assertThat(databaseRef.getName()).isEqualTo(uuid);
        assertThat(databaseRef.getHost()).isEqualTo(host);
        assertThat(databaseRef.getPort()).isEqualTo(databaseType.getDefaultPort());
        assertThat(databaseRef.getDatabaseName()).isEqualTo(databaseName);
        assertThat(databaseRef.getUser()).isEqualTo(user);
        assertThat(databaseRef.getPassword()).isEqualTo(password);
        assertThat(databaseRef.getType()).isEqualTo(databaseType);
    }

    private void assertDatabaseRef(DatabaseRef databaseRef, MockCloudServiceKey mockCloudServiceKey) {
        assertDatabaseRef(databaseRef, mockCloudServiceKey.getService().getMeta().getGuid().toString());
        assertThat(databaseRef.getDatabaseService()).isNotNull();
        assertDatabaseService(databaseRef.getDatabaseService(), mockCloudServiceKey);

    }

    private void assertDatabaseService(DatabaseService databaseService, MockCloudServiceKey mockCloudServiceKey) {
        assertThat(databaseService.getName()).isEqualTo(serviceName);
        assertThat(databaseService.getDatabaseRef()).isNotNull();
        if (databaseService.getOrg() != null) {
            assertThat(databaseService.getOrg()).isEqualTo(org);
        }
        if (databaseService.getSpace() != null) {
            assertThat(databaseService.getSpace()).isEqualTo(space);
        }
        assertThat(databaseService.getUuid()).isEqualTo(mockCloudServiceKey.getService().getMeta().getGuid().toString());
        assertThat(databaseService.getServiceKeyGuid()).isEqualTo(mockCloudServiceKey.getMeta().getGuid().toString());
    }
}