package com.orange.clara.cloud.servicedbdumper.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.security.oauth2.client.EnableOAuth2Sso;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.csrf.CsrfFilter;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.csrf.CsrfTokenRepository;
import org.springframework.security.web.csrf.HttpSessionCsrfTokenRepository;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.WebUtils;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Copyright (C) 2015 Orange
 * <p>
 * This software is distributed under the terms and conditions of the 'Apache-2.0'
 * license which can be found in the file 'LICENSE' in this package distribution
 * or at 'https://opensource.org/licenses/Apache-2.0'.
 * <p>
 * Author: Arthur Halet
 * Date: 03/06/2015
 */
@EnableWebSecurity
public class SecurityConfig {

    @Value("${broker.username:user}")
    private String brokerUsername;
    @Value("${broker.password:password}")
    private String brokerPassword;
    @Value("${admin.username:admin}")
    private String adminUsername;
    @Value("${admin.password:password}")
    private String adminPassword;

    @Value("${spring.boot.admin.username:username}")
    private String springBootAdminUsername;
    @Value("${spring.boot.admin.password:password}")
    private String springBootAdminPassword;

    @Value("${user.username:user}")
    private String defaultUserUsername;
    @Value("${user.password:password}")
    private String defaultUserPassword;


    @Autowired
    public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
        auth.inMemoryAuthentication().withUser(brokerUsername).password(brokerPassword).roles("API");
        auth.inMemoryAuthentication().withUser(defaultUserUsername).password(defaultUserPassword).roles("USER");
        auth.inMemoryAuthentication().withUser(adminUsername).password(adminPassword).roles("ADMIN", "SPRING_BOOT_ADMIN", "USER");
        auth.inMemoryAuthentication().withUser(springBootAdminUsername).password(springBootAdminPassword).roles("SPRING_BOOT_ADMIN");
    }


    @Configuration
    @Order(1)
    public static class ServiceBrokerSecurity extends WebSecurityConfigurerAdapter {
        @Value("${use.ssl:false}")
        private Boolean useSsl;

        @Override
        protected void configure(HttpSecurity http) throws Exception {
            http
                    .antMatcher("/v2/**")
                    .authorizeRequests()
                    .anyRequest()
                    .hasRole("API")
                    .and()
                    .httpBasic()
                    .and()
                    .csrf().disable();
            if (useSsl) {
                http.requiresChannel().anyRequest().requiresSecure();
            }
        }
    }

    @Configuration
    @Order(2)
    public static class DownloadSecurity extends WebSecurityConfigurerAdapter {
        @Value("${use.ssl:false}")
        private Boolean useSsl;

        @Override
        protected void configure(HttpSecurity http) throws Exception {
            http
                    .antMatcher("/manage/download/**")
                    .authorizeRequests()
                    .anyRequest()
                    .permitAll();
            if (useSsl) {
                http.requiresChannel().anyRequest().requiresSecure();
            }

        }
    }

    @Configuration
    @Order(3)
    public static class AdminSecurity extends WebSecurityConfigurerAdapter {
        @Value("${use.ssl:false}")
        private Boolean useSsl;

        @Override
        protected void configure(HttpSecurity http) throws Exception {
            http
                    .antMatcher("/admin/control/**")
                    .authorizeRequests()
                    .anyRequest()
                    .hasRole("ADMIN")
                    .and()
                    .httpBasic()
                    .and()
                    .csrf().disable();
            if (useSsl) {
                http.requiresChannel().anyRequest().requiresSecure();
            }
        }
    }

    @Configuration
    @Order(4)
    public static class AdminManagerSecurity extends WebSecurityConfigurerAdapter {
        @Value("${use.ssl:false}")
        private Boolean useSsl;

        @Override
        protected void configure(HttpSecurity http) throws Exception {
            http
                    .antMatcher("/manage/admin")
                    .authorizeRequests()
                    .anyRequest()
                    .hasRole("ADMIN")
                    .and()
                    .httpBasic()
                    .and()
                    .csrf().disable();
            if (useSsl) {
                http.requiresChannel().anyRequest().requiresSecure();
            }
        }
    }

    @Configuration
    @Order(5)
    public static class AdminMonitorSecurity extends WebSecurityConfigurerAdapter {
        @Value("${use.ssl:false}")
        private Boolean useSsl;

        @Override
        protected void configure(HttpSecurity http) throws Exception {
            http
                    .antMatcher("/admin/**")
                    .authorizeRequests()
                    .anyRequest()
                    .hasRole("SPRING_BOOT_ADMIN")
                    .and()
                    .httpBasic()
                    .and()
                    .csrf().disable();
            if (useSsl) {
                http.requiresChannel().anyRequest().requiresSecure();
            }

        }
    }

    @Configuration
    @Profile(value = "uaa")
    @EnableOAuth2Sso
    public static class InterfaceSecurity extends WebSecurityConfigurerAdapter {
        @Value("${use.ssl:false}")
        private Boolean useSsl;

        private Filter csrfHeaderFilter() {
            return new OncePerRequestFilter() {
                @Override
                protected void doFilterInternal(HttpServletRequest request,
                                                HttpServletResponse response, FilterChain filterChain)
                        throws ServletException, IOException {
                    CsrfToken csrf = (CsrfToken) request.getAttribute(CsrfToken.class
                            .getName());
                    if (csrf != null) {
                        Cookie cookie = WebUtils.getCookie(request, "XSRF-TOKEN");
                        String token = csrf.getToken();
                        if (cookie == null || token != null
                                && !token.equals(cookie.getValue())) {
                            cookie = new Cookie("XSRF-TOKEN", token);
                            cookie.setPath("/");
                            response.addCookie(cookie);
                        }
                    }
                    filterChain.doFilter(request, response);
                }
            };
        }

        private CsrfTokenRepository csrfTokenRepository() {
            HttpSessionCsrfTokenRepository repository = new HttpSessionCsrfTokenRepository();
            repository.setHeaderName("X-XSRF-TOKEN");
            return repository;
        }

        @Override
        protected void configure(HttpSecurity http) throws Exception {
            http
                    .authorizeRequests()
                    .anyRequest()
                    .authenticated()
                    .and()
                    .csrf()
                    .csrfTokenRepository(csrfTokenRepository()).and()
                    .addFilterAfter(csrfHeaderFilter(), CsrfFilter.class);
            if (useSsl) {
                http.requiresChannel().anyRequest().requiresSecure();
            }
        }
    }

    @Configuration
    @Profile("!uaa")
    public static class NoUaaSecurity extends WebSecurityConfigurerAdapter {
        @Value("${use.ssl:false}")
        private Boolean useSsl;

        @Override
        protected void configure(HttpSecurity http) throws Exception {
            http
                    .authorizeRequests()
                    .anyRequest().authenticated()
                    .and()
                    .httpBasic();
            if (useSsl) {
                http.requiresChannel().anyRequest().requiresSecure();
            }
        }
    }
}
