package com.orange.clara.cloud.servicedbdumper.service.servicekey;

import com.orange.clara.cloud.servicedbdumper.cloudfoundry.CloudFoundryClientFactory;
import com.orange.clara.cloud.servicedbdumper.fake.cloudfoundry.CloudFoundryClientFake;
import com.orange.clara.cloud.servicedbdumper.exception.ServiceKeyException;
import com.orange.clara.cloud.servicedbdumper.model.DatabaseService;
import org.cloudfoundry.client.lib.CloudFoundryClient;
import org.cloudfoundry.client.lib.domain.CloudEntity;
import org.cloudfoundry.client.lib.domain.CloudService;
import org.cloudfoundry.client.lib.domain.CloudServiceKey;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;
import java.util.UUID;

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
public class CloudFoundryServiceKeyManagerTest {

    private final static String cloudServiceKeyName = "service-key-1";
    private final static String cloudServiceName = "my-super-service";
    private final static CloudServiceKey cloudServiceKey = new CloudServiceKey(new CloudEntity.Meta(UUID.randomUUID(), new Date(), new Date()), cloudServiceKeyName);
    private final static CloudFoundryClientFake cloudFoundryClientFake = new CloudFoundryClientFake();
    private final static String databaseServiceUuid = "01";
    private final static DatabaseService databaseService = new DatabaseService(databaseServiceUuid, "name", "org", "space");
    @InjectMocks
    CloudFoundryServiceKeyManager cloudFoundryServiceKeyManager;
    @Mock
    CloudFoundryClient cloudFoundryClient;
    @Mock
    CloudFoundryClientFactory cloudFoundryClientFactory;

    @Before
    public void init() throws MalformedURLException {
        initMocks(this);
        when(cloudFoundryClient.createServiceKey((CloudService) notNull(), anyString())).thenReturn(cloudServiceKey);
        when(cloudFoundryClient.createServiceKey(anyString(), anyString())).thenReturn(cloudServiceKey);
        when(cloudFoundryClient.getCloudControllerUrl()).thenReturn(new URL("http://fake.api.cloudfoundry.com"));
        when(cloudFoundryClientFactory.createCloudFoundryClient(anyString(), (URL) notNull())).thenReturn(cloudFoundryClientFake);
        when(cloudFoundryClientFactory.createCloudFoundryClient(anyString(), (URL) notNull(), anyString(), anyString())).thenReturn(cloudFoundryClientFake);
    }

    @Test
    public void when_getting_user_service_it_should_give_the_correct_one_or_null_if_user_dont_have_access() {
        CloudService cloudService = cloudFoundryServiceKeyManager.getUserService(cloudServiceName, "token", "org", "space");
        assertThat(cloudService).isNotNull();
        assertThat(cloudService.getName()).isEqualTo(cloudServiceName);

        cloudService = cloudFoundryServiceKeyManager.getUserService(cloudServiceName, "token", null, null);
        assertThat(cloudService).isNotNull();
        assertThat(cloudService.getName()).isEqualTo(cloudServiceName);

        cloudService = cloudFoundryServiceKeyManager.getUserService(CloudFoundryClientFake.SERVICE_NOT_ACCESSIBLE, "token", "org", "space");
        assertThat(cloudService).isNull();

        cloudService = cloudFoundryServiceKeyManager.getUserService(CloudFoundryClientFake.SERVICE_NOT_ACCESSIBLE, "token", null, null);
        assertThat(cloudService).isNull();
    }

    @Test
    public void when_creating_service_key_by_passing_database_service_and_user_have_access_service_it_should_give_a_service_key() throws ServiceKeyException {
        CloudServiceKey cloudServiceKey = cloudFoundryServiceKeyManager.createServiceKey(databaseService);
        assertThat(cloudServiceKey).isNotNull();
        assertThat(cloudServiceKey.getName()).isEqualTo(cloudServiceKeyName);

    }

    @Test
    public void when_creating_service_key_and_user_have_access_service_it_should_give_a_service_key() throws ServiceKeyException {
        CloudServiceKey cloudServiceKey = cloudFoundryServiceKeyManager.createServiceKey("servicename", "token", "space", "org");
        assertThat(cloudServiceKey).isNotNull();
        assertThat(cloudServiceKey.getName()).isEqualTo(cloudServiceKeyName);


        cloudServiceKey = cloudFoundryServiceKeyManager.createServiceKey("servicename", "token", null, null);
        assertThat(cloudServiceKey).isNotNull();
        assertThat(cloudServiceKey.getName()).isEqualTo(cloudServiceKeyName);
    }

    @Test
    public void when_creating_service_key_and_user_not_have_access_service_it_should_raise_an_exception() {
        try {
            this.cloudFoundryServiceKeyManager.createServiceKey(CloudFoundryClientFake.SERVICE_NOT_ACCESSIBLE, "token", "org", "space");
            fail("Should throw an ServiceKeyException");
        } catch (Exception e) {
            assertThat(e).isInstanceOf(ServiceKeyException.class);
        }
        try {
            this.cloudFoundryServiceKeyManager.createServiceKey(CloudFoundryClientFake.SERVICE_NOT_ACCESSIBLE, "token", null, null);
            fail("Should throw an ServiceKeyException");
        } catch (Exception e) {
            assertThat(e).isInstanceOf(ServiceKeyException.class);
        }
    }
}