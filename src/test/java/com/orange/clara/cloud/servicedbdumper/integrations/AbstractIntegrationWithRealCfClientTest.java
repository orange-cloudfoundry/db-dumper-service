package com.orange.clara.cloud.servicedbdumper.integrations;

import com.orange.clara.cloud.servicedbdumper.cloudfoundry.CloudFoundryClientFactory;
import com.orange.clara.cloud.servicedbdumper.exception.CannotFindDatabaseDumperException;
import com.orange.clara.cloud.servicedbdumper.exception.DatabaseExtractionException;
import com.orange.clara.cloud.servicedbdumper.exception.ServiceKeyException;
import com.orange.clara.cloud.servicedbdumper.integrations.model.DatabaseAccess;
import com.orange.clara.cloud.servicedbdumper.model.DatabaseRef;
import com.orange.clara.cloud.servicedbdumper.model.DatabaseType;
import org.cloudfoundry.client.lib.CloudFoundryClient;
import org.cloudfoundry.client.lib.domain.CloudService;
import org.cloudfoundry.client.lib.domain.CloudServiceOffering;
import org.cloudfoundry.client.lib.domain.CloudServicePlan;
import org.cloudfoundry.community.servicebroker.exception.ServiceBrokerAsyncRequiredException;
import org.cloudfoundry.community.servicebroker.exception.ServiceBrokerException;
import org.junit.After;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.web.client.HttpServerErrorException;

import java.io.File;
import java.io.IOException;
import java.util.List;

import static org.fest.assertions.Fail.fail;
import static org.junit.Assume.assumeTrue;

/**
 * Copyright (C) 2016 Orange
 * <p>
 * This software is distributed under the terms and conditions of the 'Apache-2.0'
 * license which can be found in the file 'LICENSE' in this package distribution
 * or at 'https://opensource.org/licenses/Apache-2.0'.
 * <p>
 * Author: Arthur Halet
 * Date: 08/04/2016
 */
abstract public class AbstractIntegrationWithRealCfClientTest extends AbstractIntegrationTest {

    @Value("${int.cf.service.name.mysql:cleardb}")
    protected String serviceNameMysql;
    @Value("${int.cf.service.plan.mysql:spark}")
    protected String servicePlanMysql;
    @Value("${int.cf.service.instance.source.mysql:mysql-db-dumper-src-int}")
    protected String serviceSourceInstanceMysql;
    @Value("${int.cf.service.instance.target.mysql:mysql-db-dumper-dest-int}")
    protected String serviceTargetInstanceMysql;

    @Value("${int.cf.service.name.postgres:elephantsql}")
    protected String serviceNamePostgres;
    @Value("${int.cf.service.plan.postgres:turtle}")
    protected String servicePlanPostgres;
    @Value("${int.cf.service.instance.source.postgres:postgres-db-dumper-src-int}")
    protected String serviceSourceInstancePostgres;
    @Value("${int.cf.service.instance.target.postgres:postgres-db-dumper-dest-int}")
    protected String serviceTargetInstancePostgres;

    @Value("${int.cf.service.name.mongodb:mongolab}")
    protected String serviceNameMongo;
    @Value("${int.cf.service.plan.mongodb:sandbox}")
    protected String servicePlanMongo;
    @Value("${int.cf.service.instance.source.mongodb:mongodb-db-dumper-src-int}")
    protected String serviceSourceInstanceMongo;
    @Value("${int.cf.service.instance.target.mongodb:mongodb-db-dumper-dest-int}")
    protected String serviceTargetInstanceMongo;

    @Value("${int.cf.service.name.redis:rediscloud}")
    protected String serviceNameRedis;
    @Value("${int.cf.service.plan.redis:30mb}")
    protected String servicePlanRedis;
    @Value("${int.cf.service.instance.source.redis:redis-db-dumper-src-int}")
    protected String serviceSourceInstanceRedis;
    @Value("${int.cf.service.instance.target.redis:redis-db-dumper-dest-int}")
    protected String serviceTargetInstanceRedis;


    protected CloudFoundryClient cfClientToPopulate;

    @Autowired
    protected CloudFoundryClientFactory clientFactory;
    @Autowired
    @Qualifier("cfAdminUser")
    protected String cfAdminUser;
    @Autowired
    @Qualifier("cfAdminPassword")
    protected String cfAdminPassword;
    @Autowired
    @Qualifier("cloudControllerUrl")
    protected String cloudControllerUrl;
    @Value("${int.cf.admin.org:#{null}}")
    protected String org;
    @Value("${int.cf.admin.space:#{null}}")
    protected String space;

    @Autowired
    @Qualifier("cloudFoundryClientAsAdmin")
    protected CloudFoundryClient cfAdminClient;


    @Override
    public void doBeforeTest(DatabaseType databaseType) throws DatabaseExtractionException, CannotFindDatabaseDumperException, InterruptedException, IOException {
        assumeTrue("Please define properties: cf.admin.user, cf.admin.password, cloud.controller.url to run this test. This test is skipped.", !(this.cfAdminUser == null
                || this.cfAdminUser.isEmpty()
                || this.cfAdminPassword == null
                || this.cfAdminPassword.isEmpty()
                || this.cloudControllerUrl == null
                || this.cloudControllerUrl.isEmpty()));
        if (org != null && space != null) {
            cfClientToPopulate = this.clientFactory.createCloudFoundryClient(cfAdminUser, cfAdminPassword, cloudControllerUrl, org, space);
        } else {
            cfClientToPopulate = cfAdminClient;
        }

        DatabaseAccess databaseAccess = this.databaseAccessMap.get(databaseType);
        boolean isServiceExists = isServiceExist(databaseAccess.getServiceName(), databaseAccess.getServicePlan());
        if (!isServiceExists) {
            this.skipCleaning = true;
        }
        assumeTrue(String.format("The service %s with plan %s doesn't exists please", databaseAccess.getServiceName(), databaseAccess.getServicePlan()),
                isServiceExists);

        CloudService cloudServiceSource = new CloudService(null, databaseAccess.getServiceSourceInstanceName());
        cloudServiceSource.setPlan(databaseAccess.getServicePlan());
        cloudServiceSource.setLabel(databaseAccess.getServiceName());
        try {
            cfClientToPopulate.createService(cloudServiceSource);
        } catch (HttpServerErrorException e) {
            if (!e.getStatusCode().equals(HttpStatus.BAD_GATEWAY)) {
                throw e;
            } else {
                assumeTrue("Bad gateway error, skipping test", false);
            }
        }

        if (!databaseAccess.getServiceTargetInstanceName().equals(databaseAccess.getServiceSourceInstanceName())) {
            CloudService cloudServiceTarget = new CloudService(null, databaseAccess.getServiceTargetInstanceName());
            cloudServiceTarget.setPlan(databaseAccess.getServicePlan());
            cloudServiceTarget.setLabel(databaseAccess.getServiceName());
            cfClientToPopulate.createService(cloudServiceTarget);
        }


        OAuth2AccessToken accessToken = cfClientToPopulate.login();
        this.requestForge.setUserToken(accessToken.getValue());
        this.requestForge.setOrg(org);
        this.requestForge.setSpace(space);
        super.doBeforeTest(databaseType);
    }

    @Override
    public void cleanDatabase(DatabaseType databaseType) throws DatabaseExtractionException, CannotFindDatabaseDumperException, InterruptedException, IOException {

    }

    @Override
    @After
    public void cleanAfterTest() throws DatabaseExtractionException, CannotFindDatabaseDumperException, InterruptedException, IOException, ServiceBrokerAsyncRequiredException, ServiceBrokerException {
        if (this.cfAdminUser == null
                || this.cfAdminUser.isEmpty()
                || this.cfAdminPassword == null
                || this.cfAdminPassword.isEmpty()
                || this.cloudControllerUrl == null
                || this.cloudControllerUrl.isEmpty()) {
            return;
        }
        for (DatabaseType databaseType : this.databaseAccessMap.keySet()) {
            DatabaseAccess databaseAccess = this.databaseAccessMap.get(databaseType);
            this.cfClientToPopulate.deleteService(databaseAccess.getServiceSourceInstanceName());
            this.cfClientToPopulate.deleteService(databaseAccess.getServiceTargetInstanceName());
        }
        if (this.skipCleaning) {
            return;
        }
        this.requestForge.createDefaultData();
        super.cleanAfterTest();
    }

    @Override
    protected boolean isServerListening(DatabaseType databaseType) throws DatabaseExtractionException {
        DatabaseAccess databaseAccess = this.databaseAccessMap.get(databaseType);
        DatabaseRef sourceDatabase = null;
        DatabaseRef targetDatabase = null;
        try {
            sourceDatabase = this.databaseRefManager.getDatabaseRef(databaseAccess.getServiceSourceInstanceName(), requestForge.getUserToken(), requestForge.getOrg(), requestForge.getSpace());
            targetDatabase = this.databaseRefManager.getDatabaseRef(databaseAccess.getServiceTargetInstanceName(), requestForge.getUserToken(), requestForge.getOrg(), requestForge.getSpace());
        } catch (ServiceKeyException e) {
            throw new DatabaseExtractionException(e.getMessage(), e);
        }
        boolean result = this.isSocketOpen(sourceDatabase.getHost(), sourceDatabase.getPort()) &&
                this.isSocketOpen(targetDatabase.getHost(), targetDatabase.getPort());
        this.databaseRefManager.deleteServiceKey(sourceDatabase);
        this.databaseRefManager.deleteServiceKey(targetDatabase);
        return result;
    }

    @Override
    public void populateData(DatabaseType databaseType) throws DatabaseExtractionException, CannotFindDatabaseDumperException, IOException, InterruptedException {
        DatabaseAccess databaseAccess = this.databaseAccessMap.get(databaseType);
        File fakeData = databaseAccess.getFakeDataFile();
        if (fakeData == null) {
            fail("Cannot find file for database: " + databaseType);
            return;
        }
        DatabaseRef sourceDatabase = null;
        try {
            sourceDatabase = this.databaseRefManager.getDatabaseRef(databaseAccess.getServiceSourceInstanceName(), requestForge.getUserToken(), requestForge.getOrg(), requestForge.getSpace());
        } catch (ServiceKeyException e) {
            throw new DatabaseExtractionException(e.getMessage(), e);
        }
        this.populateDataToDatabaseRefFromFile(fakeData, sourceDatabase);
        this.databaseRefManager.deleteServiceKey(sourceDatabase);
    }

    @Override
    protected void populateDatabaseAccessMap() throws DatabaseExtractionException {
        super.populateDatabaseAccessMap();
        DatabaseAccess mysqlAccess = this.databaseAccessMap.get(DatabaseType.MYSQL);
        mysqlAccess.setServiceName(serviceNameMysql);
        mysqlAccess.setServicePlan(servicePlanMysql);
        mysqlAccess.setServiceSourceInstanceName(serviceSourceInstanceMysql);
        mysqlAccess.setServiceTargetInstanceName(serviceTargetInstanceMysql);

        DatabaseAccess postgresAccess = this.databaseAccessMap.get(DatabaseType.POSTGRESQL);
        postgresAccess.setServiceName(serviceNamePostgres);
        postgresAccess.setServicePlan(servicePlanPostgres);
        postgresAccess.setServiceSourceInstanceName(serviceSourceInstancePostgres);
        postgresAccess.setServiceTargetInstanceName(serviceTargetInstancePostgres);

        DatabaseAccess mongoAccess = this.databaseAccessMap.get(DatabaseType.MONGODB);
        mongoAccess.setServiceName(serviceNameMongo);
        mongoAccess.setServicePlan(servicePlanMongo);
        mongoAccess.setServiceSourceInstanceName(serviceSourceInstanceMongo);
        mongoAccess.setServiceTargetInstanceName(serviceTargetInstanceMongo);

        DatabaseAccess redisAccess = this.databaseAccessMap.get(DatabaseType.REDIS);
        redisAccess.setServiceName(serviceNameRedis);
        redisAccess.setServicePlan(servicePlanRedis);
        redisAccess.setServiceSourceInstanceName(serviceSourceInstanceRedis);
        if (serviceNameRedis.equals("rediscloud")) {
            redisAccess.setServiceTargetInstanceName(serviceSourceInstanceRedis);
        } else {
            redisAccess.setServiceTargetInstanceName(serviceTargetInstanceRedis);
        }

    }

    public boolean isServiceExist(String serviceName, String plan) {
        List<CloudServiceOffering> offeringList = cfClientToPopulate.getServiceOfferings();
        for (CloudServiceOffering offering : offeringList) {
            if (!offering.getName().equals(serviceName)) {
                continue;
            }
            for (CloudServicePlan servicePlan : offering.getCloudServicePlans()) {
                if (servicePlan.getName().equals(plan)) {

                    return true;
                }
            }
        }
        return false;
    }
}
