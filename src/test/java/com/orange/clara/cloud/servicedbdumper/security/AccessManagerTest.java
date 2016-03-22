package com.orange.clara.cloud.servicedbdumper.security;

import com.google.common.collect.Lists;
import com.orange.clara.cloud.servicedbdumper.exception.UserAccessRightException;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Spy;
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
 * Date: 22/03/2016
 */
public class AccessManagerTest {
    @Spy
    AccessManager accessManager;

    @Before
    public void init() {
        initMocks(this);
    }

    @Test
    public void when_check_if_user_is_admin_and_there_is_no_security_context_it_should_return_that_user_is_not_an_admin() throws UserAccessRightException {
        assertThat(accessManager.isUserIsAdmin()).isFalse();

        when(accessManager.getSecurityContextHolder()).thenReturn(new SecurityContext() {
            @Override
            public Authentication getAuthentication() {
                return null;
            }

            @Override
            public void setAuthentication(Authentication authentication) {

            }
        });
        assertThat(accessManager.isUserIsAdmin()).isFalse();
    }

    @Test
    public void when_check_if_user_is_admin_and_user_has_not_admin_authority_it_should_return_that_user_is_not_an_admin() throws UserAccessRightException {
        this.injectUser();
        assertThat(accessManager.isUserIsAdmin()).isFalse();
    }

    @Test
    public void when_check_if_user_is_admin_and_user_has_admin_authority_it_should_return_that_user_is_an_admin() throws UserAccessRightException {
        this.injectAdmin();
        assertThat(accessManager.isUserIsAdmin()).isTrue();
    }

    private void injectAdmin() {
        when(accessManager.getSecurityContextHolder()).thenReturn(new SecurityContext() {
            @Override
            public Authentication getAuthentication() {
                return new Authentication() {
                    @Override
                    public Collection<? extends GrantedAuthority> getAuthorities() {
                        List<GrantedAuthority> grantedAuthorities = Lists.newArrayList();
                        grantedAuthorities.add(new SimpleGrantedAuthority(AccessManager.AUTHORIZED_AUTHORITY));
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
        });
    }

    private void injectUser() {
        when(accessManager.getSecurityContextHolder()).thenReturn(new SecurityContext() {
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
        });
    }
}