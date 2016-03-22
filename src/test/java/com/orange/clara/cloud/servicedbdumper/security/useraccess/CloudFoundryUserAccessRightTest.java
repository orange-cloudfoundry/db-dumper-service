package com.orange.clara.cloud.servicedbdumper.security.useraccess;

import com.orange.clara.cloud.servicedbdumper.dbdumper.fake.services.FakeAccessManager;
import com.orange.clara.cloud.servicedbdumper.exception.UserAccessRightException;
import com.orange.clara.cloud.servicedbdumper.model.DatabaseRef;
import com.orange.clara.cloud.servicedbdumper.model.DbDumperServiceInstance;
import org.cloudfoundry.client.lib.CloudFoundryClient;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import static org.fest.assertions.Assertions.assertThat;
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
 * Date: 17/03/2016
 */
public class CloudFoundryUserAccessRightTest {
    private final static String authorizedInstanceId = "1";
    private final static String unauthorizedInstanceId = "2";
    private final static DbDumperServiceInstance authorizedServiceInstance = new DbDumperServiceInstance();
    private final static DbDumperServiceInstance unauthorizedServiceInstance = new DbDumperServiceInstance();

    @InjectMocks
    CloudFoundryUserAccessRight cloudFoundryUserAccessRight;

    @Mock
    CloudFoundryClient cloudFoundryClient;

    @Before
    public void init() {
        initMocks(this);
        authorizedServiceInstance.setServiceInstanceId(authorizedInstanceId);
        unauthorizedServiceInstance.setServiceInstanceId(unauthorizedInstanceId);
        when(cloudFoundryClient.checkUserPermission(authorizedInstanceId)).thenReturn(true);
        when(cloudFoundryClient.checkUserPermission(unauthorizedInstanceId)).thenReturn(false);
        cloudFoundryUserAccessRight.accessManager = new FakeAccessManager(false);
    }


    @Test
    public void ensure_check_is_correct_when_user_connected() throws UserAccessRightException {
        //direct id
        assertThat(cloudFoundryUserAccessRight.haveAccessToServiceInstance(authorizedInstanceId)).isTrue();
        assertThat(cloudFoundryUserAccessRight.haveAccessToServiceInstance(unauthorizedInstanceId)).isFalse();

        // when giving service instance
        assertThat(cloudFoundryUserAccessRight.haveAccessToServiceInstance(authorizedServiceInstance)).isTrue();
        assertThat(cloudFoundryUserAccessRight.haveAccessToServiceInstance(unauthorizedInstanceId)).isFalse();

    }

    @Test
    public void when_database_ref_is_checked_and_at_least_one_service_instance_have_access_it_should_give_access() throws UserAccessRightException {
        DatabaseRef databaseRef = new DatabaseRef();
        databaseRef.addDbDumperServiceInstance(authorizedServiceInstance);
        databaseRef.addDbDumperServiceInstance(unauthorizedServiceInstance);

        assertThat(cloudFoundryUserAccessRight.haveAccessToServiceInstance(databaseRef)).isTrue();
    }

    @Test
    public void when_database_ref_is_checked_and_no_service_instance_have_access_it_should_not_give_access() throws UserAccessRightException {
        DatabaseRef databaseRef = new DatabaseRef();
        databaseRef.addDbDumperServiceInstance(unauthorizedServiceInstance);

        assertThat(cloudFoundryUserAccessRight.haveAccessToServiceInstance(databaseRef)).isFalse();
    }

    @Test
    public void when_admin_is_connected_it_should_give_him_all_access() throws UserAccessRightException {
        cloudFoundryUserAccessRight.accessManager = new FakeAccessManager(true);

        assertThat(cloudFoundryUserAccessRight.haveAccessToServiceInstance(authorizedInstanceId)).isTrue();
        assertThat(cloudFoundryUserAccessRight.haveAccessToServiceInstance(unauthorizedServiceInstance)).isTrue();
    }


}