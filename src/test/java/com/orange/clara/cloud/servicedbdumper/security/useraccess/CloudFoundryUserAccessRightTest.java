package com.orange.clara.cloud.servicedbdumper.security.useraccess;

import com.google.common.collect.Lists;
import com.orange.clara.cloud.servicedbdumper.exception.UserAccessRightException;
import com.orange.clara.cloud.servicedbdumper.model.DatabaseRef;
import com.orange.clara.cloud.servicedbdumper.model.DbDumperServiceInstance;
import org.cloudfoundry.client.lib.CloudFoundryClient;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;

import java.util.Collection;
import java.util.List;

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
        this.injectUser();
    }

    @Test
    public void when_check_access_for_service_id_and_no_security_context_it_should_check_directly_from_cloudfoundryclient() throws UserAccessRightException {
        cloudFoundryUserAccessRight.securityContext = null;
        assertThat(cloudFoundryUserAccessRight.haveAccessToServiceInstance(authorizedInstanceId)).isTrue();
        assertThat(cloudFoundryUserAccessRight.haveAccessToServiceInstance(unauthorizedInstanceId)).isFalse();

        cloudFoundryUserAccessRight.securityContext = new SecurityContext() {
            @Override
            public Authentication getAuthentication() {
                return null;
            }

            @Override
            public void setAuthentication(Authentication authentication) {

            }
        };
        assertThat(cloudFoundryUserAccessRight.haveAccessToServiceInstance(authorizedInstanceId)).isTrue();
        assertThat(cloudFoundryUserAccessRight.haveAccessToServiceInstance(unauthorizedInstanceId)).isFalse();
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
        this.injectAdmin();
        assertThat(cloudFoundryUserAccessRight.haveAccessToServiceInstance(authorizedInstanceId)).isTrue();
        assertThat(cloudFoundryUserAccessRight.haveAccessToServiceInstance(unauthorizedServiceInstance)).isTrue();
    }

    private void injectAdmin() {
        cloudFoundryUserAccessRight.securityContext = new SecurityContext() {
            @Override
            public Authentication getAuthentication() {
                return new Authentication() {
                    @Override
                    public Collection<? extends GrantedAuthority> getAuthorities() {
                        List<GrantedAuthority> grantedAuthorities = Lists.newArrayList();
                        grantedAuthorities.add(new SimpleGrantedAuthority(CloudFoundryUserAccessRight.AUTHORIZED_AUTHORITY));
                        return grantedAuthorities;
                    }

                    @Override
                    public String getName() {
                        return null;
                    }

                    @Override
                    public Object getCredentials() {
                        return null;
                    }

                    @Override
                    public Object getDetails() {
                        return null;
                    }

                    @Override
                    public Object getPrincipal() {
                        return null;
                    }

                    @Override
                    public boolean isAuthenticated() {
                        return false;
                    }

                    @Override
                    public void setAuthenticated(boolean b) throws IllegalArgumentException {

                    }


                };
            }

            @Override
            public void setAuthentication(Authentication authentication) {

            }
        };
    }

    private void injectUser() {
        cloudFoundryUserAccessRight.securityContext = new SecurityContext() {
            @Override
            public Authentication getAuthentication() {
                return new Authentication() {
                    @Override
                    public Collection<? extends GrantedAuthority> getAuthorities() {
                        List<GrantedAuthority> grantedAuthorities = Lists.newArrayList();
                        grantedAuthorities.add(new SimpleGrantedAuthority("USER"));
                        return grantedAuthorities;
                    }

                    @Override
                    public Object getCredentials() {
                        return null;
                    }

                    @Override
                    public Object getDetails() {
                        return null;
                    }

                    @Override
                    public Object getPrincipal() {
                        return null;
                    }

                    @Override
                    public boolean isAuthenticated() {
                        return false;
                    }

                    @Override
                    public void setAuthenticated(boolean b) throws IllegalArgumentException {

                    }

                    @Override
                    public String getName() {
                        return null;
                    }
                };
            }

            @Override
            public void setAuthentication(Authentication authentication) {

            }
        };
    }
}