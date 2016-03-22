package com.orange.clara.cloud.servicedbdumper.security;

import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Collection;

/**
 * Copyright (C) 2016 Orange
 * <p>
 * This software is distributed under the terms and conditions of the 'Apache-2.0'
 * license which can be found in the file 'LICENSE' in this package distribution
 * or at 'https://opensource.org/licenses/Apache-2.0'.
 * <p>
 * Author: Arthur Halet
 * Date: 22/03/2016
 */
@Service
@Scope(value = "session", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class AccessManager {
    public final static String AUTHORIZED_AUTHORITY = "ROLE_ADMIN";

    public boolean isUserIsAdmin() {
        SecurityContext context = this.getSecurityContextHolder();
        if (context == null) {
            System.out.println("no context");
            return false;
        }
        Authentication authentication = context.getAuthentication();
        if (authentication == null) {
            System.out.println("no authent");
            return false;
        }
        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        System.out.println("authorities " + authorities);
        return authorities.contains(new SimpleGrantedAuthority(AUTHORIZED_AUTHORITY));
    }

    protected SecurityContext getSecurityContextHolder() {
        return SecurityContextHolder.getContext();
    }
}
