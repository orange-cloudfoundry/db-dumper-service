package com.orange.clara.cloud.servicedbdumper.integrations;

import com.orange.clara.cloud.servicedbdumper.Application;
import com.orange.clara.cloud.servicedbdumper.exception.CannotFindDatabaseDumperException;
import com.orange.clara.cloud.servicedbdumper.exception.DatabaseExtractionException;
import com.orange.clara.cloud.servicedbdumper.exception.ServiceKeyException;
import com.orange.clara.cloud.servicedbdumper.integrations.config.FakeCloudFoundryClientConfig;
import com.orange.clara.cloud.servicedbdumper.model.DatabaseType;
import com.orange.clara.cloud.servicedbdumper.model.DbDumperServiceInstance;
import com.orange.clara.cloud.servicedbdumper.repo.DatabaseRefRepo;
import com.orange.clara.cloud.servicedbdumper.utiltest.ReportIntegration;
import org.cloudfoundry.community.servicebroker.exception.*;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.boot.test.WebIntegrationTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.IOException;

import static org.fest.assertions.Assertions.assertThat;

/**
 * Copyright (C) 2016 Orange
 * <p>
 * This software is distributed under the terms and conditions of the 'Apache-2.0'
 * license which can be found in the file 'LICENSE' in this package distribution
 * or at 'https://opensource.org/licenses/Apache-2.0'.
 * <p>
 * Author: Arthur Halet
 * Date: 19/08/2016
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration({Application.class, FakeCloudFoundryClientConfig.class})
@WebIntegrationTest(randomPort = true)
@ActiveProfiles({"local", "cloud", "integration", "integration-fake-cf-client"})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class DeleteAfterPeriodDumpFromUriIT extends DumpAndRestoreDatabaseFromServiceNameToUriWithFakeCloudFoundryClientIT {


    @Autowired
    protected DatabaseRefRepo databaseRefRepo;

    @Override
    @Before
    public void init() throws DatabaseExtractionException {
        System.setProperty("dump.delete.expiration.days", "1");
        this.reportIntegration = new ReportIntegration("fake to not break");
        super.init();
    }

    @Override
    @Test
    @Ignore("We use the test framework for dump and restore but we don't want to run those tests")
    public void when_dump_and_restore_a_MYSQL_database_it_should_have_the_database_source_equals_to_the_database_target() throws DatabaseExtractionException, CannotFindDatabaseDumperException, InterruptedException, IOException, ServiceInstanceUpdateNotSupportedException, ServiceBrokerAsyncRequiredException, ServiceBrokerException, ServiceInstanceDoesNotExistException, ServiceKeyException, ServiceInstanceExistsException {

    }

    @Override
    @Test
    @Ignore("We use the test framework for dump and restore but we don't want to run those tests")
    public void when_dump_and_restore_a_POSTGRES_database_it_should_have_the_database_source_equals_to_the_database_target() throws DatabaseExtractionException, CannotFindDatabaseDumperException, InterruptedException, IOException, ServiceBrokerException, ServiceInstanceExistsException, ServiceBrokerAsyncRequiredException, ServiceInstanceUpdateNotSupportedException, ServiceInstanceDoesNotExistException, ServiceKeyException {

    }

    @Override
    @Test
    @Ignore("We use the test framework for dump and restore but we don't want to run those tests")
    public void when_dump_and_restore_a_REDIS_database_it_should_have_the_database_source_equals_to_the_database_target() throws DatabaseExtractionException, CannotFindDatabaseDumperException, InterruptedException, IOException, ServiceInstanceUpdateNotSupportedException, ServiceBrokerAsyncRequiredException, ServiceBrokerException, ServiceInstanceDoesNotExistException, ServiceKeyException, ServiceInstanceExistsException {

    }

    @Override
    @Test
    @Ignore("We use the test framework for dump and restore but we don't want to run those tests")
    public void when_dump_and_restore_a_MONGODB_database_it_should_have_the_database_source_equals_to_the_database_target() throws DatabaseExtractionException, CannotFindDatabaseDumperException, InterruptedException, IOException, ServiceInstanceUpdateNotSupportedException, ServiceBrokerAsyncRequiredException, ServiceBrokerException, ServiceInstanceDoesNotExistException, ServiceKeyException, ServiceInstanceExistsException {

    }

    @Test
    public void when_user_delete_service_and_recreating_service_it_should_be_possible_to_get_back_dump_before_a_period() throws DatabaseExtractionException, CannotFindDatabaseDumperException, InterruptedException, IOException, ServiceInstanceUpdateNotSupportedException, ServiceBrokerAsyncRequiredException, ServiceBrokerException, ServiceInstanceDoesNotExistException, ServiceKeyException, ServiceInstanceExistsException {
        DatabaseType databaseType = DatabaseType.MYSQL;

        this.loadServiceIds(databaseType);
        this.serviceIdTarget = null;
        this.currentDatabaseType = databaseType;
        this.doBeforeTest(databaseType);

        this.loadBeforeAction();
        createSourceDatabaseDump(databaseType);
        logger.info("Dump database source finished");

        assertThat(this.serviceInstanceRepo.exists(serviceIdSource)).isTrue();
        DbDumperServiceInstance dbDumperServiceInstance = this.serviceInstanceRepo.findOne(serviceIdSource);
        String databaseRefSourceName = dbDumperServiceInstance.getDatabaseRef().getName();

        this.deleteServiceInstance(serviceIdSource);
        logger.info("Service {} deleted", serviceIdSource);

        assertThat(this.databaseRefRepo.exists(databaseRefSourceName)).isTrue();
        dbDumperServiceInstance = this.serviceInstanceRepo.findOne(serviceIdSource);
        assertThat(dbDumperServiceInstance.getDeleted()).isTrue();
        assertThat(dbDumperServiceInstance.getDatabaseDumpFiles()).hasSize(1);
        assertThat(dbDumperServiceInstance.getDatabaseDumpFiles().get(0).getDeleted()).isFalse();

        this.loadBeforeAction();
        createSourceDatabaseDump(databaseType);
        logger.info("Dump database source after deletion finished");

        assertThat(this.databaseRefRepo.exists(databaseRefSourceName)).isTrue();
        dbDumperServiceInstance = this.serviceInstanceRepo.findOne(serviceIdSource);
        assertThat(dbDumperServiceInstance.getDeleted()).isFalse();
        assertThat(dbDumperServiceInstance.getDatabaseDumpFiles()).hasSize(2);
        assertThat(dbDumperServiceInstance.getDatabaseDumpFiles().get(0).getDeleted()).isFalse();
        assertThat(dbDumperServiceInstance.getDatabaseDumpFiles().get(1).getDeleted()).isFalse();
    }
}
