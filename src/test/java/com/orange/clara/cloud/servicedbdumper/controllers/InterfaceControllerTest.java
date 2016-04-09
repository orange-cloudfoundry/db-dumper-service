package com.orange.clara.cloud.servicedbdumper.controllers;

import com.orange.clara.cloud.servicedbdumper.Application;
import com.orange.clara.cloud.servicedbdumper.config.Routes;
import com.orange.clara.cloud.servicedbdumper.filer.Filer;
import com.orange.clara.cloud.servicedbdumper.helper.UrlForge;
import com.orange.clara.cloud.servicedbdumper.model.DatabaseRef;
import com.orange.clara.cloud.servicedbdumper.model.DbDumperPlan;
import com.orange.clara.cloud.servicedbdumper.model.DbDumperServiceInstance;
import com.orange.clara.cloud.servicedbdumper.repo.DatabaseRefRepo;
import com.orange.clara.cloud.servicedbdumper.repo.DbDumperPlanRepo;
import com.orange.clara.cloud.servicedbdumper.repo.DbDumperServiceInstanceRepo;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;

import java.net.URI;
import java.util.Arrays;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

/**
 * Copyright (C) 2016 Arthur Halet
 * <p>
 * This software is distributed under the terms and conditions of the 'MIT'
 * license which can be found in the file 'LICENSE' in this package distribution
 * or at 'http://opensource.org/licenses/MIT'.
 * <p>
 * Author: Arthur Halet
 * Date: 23/03/2016
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = {Application.class})
@WebAppConfiguration
@ActiveProfiles({"local", "test-controller", "integration"})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class InterfaceControllerTest {
    private final static String databaseName1 = "database-1";
    private final static String databaseName2 = "database-2";
    private final static String serviceId1 = "service-1";
    private final static String serviceId2 = "service-2";
    @Autowired
    Filer filer;
    private DbDumperServiceInstance serviceInstance1;
    private DbDumperServiceInstance serviceInstance2;
    private MockMvc mockMvc;
    @Autowired
    private WebApplicationContext webApplicationContext;
    @Autowired
    private DatabaseRefRepo databaseRefRepo;
    @Autowired
    private DbDumperServiceInstanceRepo instanceRepository;
    @Autowired
    private DbDumperPlanRepo dbDumperPlanRepo;

    @Before
    public void setup() throws Exception {
        this.mockMvc = webAppContextSetup(webApplicationContext).build();
        Iterable<DbDumperPlan> dbDumperPlans = dbDumperPlanRepo.findAll();
        DbDumperPlan dbDumperPlan = dbDumperPlans.iterator().next();
        serviceInstance1 = new DbDumperServiceInstance(serviceId1, "plan-1", "org-1", "space-1", "http://dashboard.com", dbDumperPlan);
        serviceInstance2 = new DbDumperServiceInstance(serviceId2, "plan-2", "org-2", "space-2", "http://dashboard.com", dbDumperPlan);
        DatabaseRef databaseRef1 = new DatabaseRef(databaseName1, URI.create("mysql://foo:bar@mymysql-1/mydb"));
        DatabaseRef databaseRef2 = new DatabaseRef(databaseName2, URI.create("mysql://foo:bar@mymysql-2/mydb"));
        databaseRefRepo.save(Arrays.asList(
                databaseRef1,
                databaseRef2
        ));
        serviceInstance1.setDatabaseRef(databaseRef1);
        serviceInstance2.setDatabaseRef(databaseRef2);
        instanceRepository.save(Arrays.asList(
                serviceInstance1,
                serviceInstance2
        ));
    }

    @Test
    public void when_asking_to_have_the_list_of_databases_it_should_return_all_databases() throws Exception {
        mockMvc.perform(get(Routes.MANAGE_ROOT + Routes.MANAGE_LIST))
                .andExpect(model().attribute("databaseRefs", hasItem(
                        allOf(
                                hasProperty("name", is(databaseName1))
                        )
                )))
                .andExpect(model().attribute("databaseRefs", hasItem(
                        allOf(
                                hasProperty("name", is(databaseName2))
                        )
                )))
                .andExpect(model().attribute("urlForge", instanceOf(UrlForge.class)))
                .andExpect(model().attribute("currency", not(isEmptyOrNullString())))
                .andExpect(model().attribute("isFree", is(true)));
    }

    @Test
    public void when_asking_to_have_the_list_of_databases_from_service_instance_it_should_return_only_database_from_service_instance() throws Exception {
        mockMvc.perform(get(Routes.MANAGE_ROOT + Routes.MANAGE_LIST + "/" + serviceInstance1.getServiceInstanceId()))
                .andExpect(model().attribute("databaseRefs", hasItem(
                        allOf(
                                hasProperty("name", is(databaseName1))
                        )
                )))
                .andExpect(model().attribute("databaseRefs", not(hasItem(
                        allOf(
                                hasProperty("name", is(databaseName2))
                        )
                ))))
                .andExpect(model().attribute("urlForge", instanceOf(UrlForge.class)))
                .andExpect(model().attribute("currency", not(isEmptyOrNullString())))
                .andExpect(model().attribute("isFree", is(true)));
    }

    @Test
    public void when_asking_to_have_a_databases_it_should_return_only_this_database() throws Exception {
        mockMvc.perform(get(Routes.MANAGE_ROOT + Routes.MANAGE_LIST_DATABASE_ROOT + "/" + serviceInstance2.getDatabaseRef().getName()))
                .andExpect(model().attribute("databaseRefs", not(hasItem(
                        allOf(
                                hasProperty("name", is(databaseName1))
                        )
                ))))
                .andExpect(model().attribute("databaseRefs", hasItem(
                        allOf(
                                hasProperty("name", is(databaseName2))
                        )
                )))
                .andExpect(model().attribute("urlForge", instanceOf(UrlForge.class)))
                .andExpect(model().attribute("currency", not(isEmptyOrNullString())))
                .andExpect(model().attribute("isFree", is(true)));
    }
}