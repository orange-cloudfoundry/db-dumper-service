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
import org.cloudfoundry.client.lib.domain.CloudServiceKey;
import org.cloudfoundry.client.lib.domain.CloudServiceOffering;
import org.cloudfoundry.client.lib.domain.CloudServicePlan;
import org.cloudfoundry.community.servicebroker.exception.ServiceBrokerAsyncRequiredException;
import org.cloudfoundry.community.servicebroker.exception.ServiceBrokerException;
import org.junit.After;
import org.junit.Before;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.web.client.HttpServerErrorException;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.*;

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

    @Value("${int.cf.service.name.postgresql:elephantsql}")
    protected String serviceNamePostgres;
    @Value("${int.cf.service.plan.postgresql:turtle}")
    protected String servicePlanPostgres;
    @Value("${int.cf.service.instance.source.postgresql:postgres-db-dumper-src-int}")
    protected String serviceSourceInstancePostgres;
    @Value("${int.cf.service.instance.target.postgresql:postgres-db-dumper-dest-int}")
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
    @Value("${test.cf.admin.org:#{null}}")
    protected String org;
    @Value("${test.cf.admin.space:#{null}}")
    protected String space;
    @Value("${test.timeout.creating.service:5}")
    protected int timeoutCreatingService;
    @Autowired
    @Qualifier("cloudFoundryClientAsAdmin")
    protected CloudFoundryClient cfAdminClient;

    @Override
    @Before
    public void init() throws DatabaseExtractionException {
        super.init();
    }

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
        assumeTrue(String.format("The service %s with plan %s doesn't exists please set properties 'test.cf.service.name.%s' and 'test.cf.service.plan.%s'",
                databaseAccess.getServiceName(),
                databaseAccess.getServicePlan(),
                databaseAccess.generateDatabaseRef().getType().toString().toLowerCase(),
                databaseAccess.generateDatabaseRef().getType().toString().toLowerCase()
                ),
                isServiceExists);

        CloudService cloudServiceSource = new CloudService(null, databaseAccess.getServiceSourceInstanceName());
        cloudServiceSource.setPlan(databaseAccess.getServicePlan());
        cloudServiceSource.setLabel(databaseAccess.getServiceName());
        this.createService(cloudServiceSource);
        if (!databaseAccess.getServiceTargetInstanceName().equals(databaseAccess.getServiceSourceInstanceName())) {
            CloudService cloudServiceTarget = new CloudService(null, databaseAccess.getServiceTargetInstanceName());
            cloudServiceTarget.setPlan(databaseAccess.getServicePlan());
            cloudServiceTarget.setLabel(databaseAccess.getServiceName());
            this.createService(cloudServiceTarget);
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
            List<CloudServiceKey> cloudServiceKeys = this.cfClientToPopulate.getServiceKeys();
            for (CloudServiceKey cloudServiceKey : cloudServiceKeys) {
                if (cloudServiceKey.getService().getName().equals(databaseAccess.getServiceSourceInstanceName())
                        || cloudServiceKey.getService().getName().equals(databaseAccess.getServiceTargetInstanceName())) {
                    this.cfClientToPopulate.deleteServiceKey(cloudServiceKey);
                }
            }
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
        mysqlAccess.setServiceSourceInstanceName(this.generateServiceName(serviceSourceInstanceMysql));
        mysqlAccess.setServiceTargetInstanceName(this.generateServiceName(serviceTargetInstanceMysql));

        DatabaseAccess postgresAccess = this.databaseAccessMap.get(DatabaseType.POSTGRESQL);
        postgresAccess.setServiceName(serviceNamePostgres);
        postgresAccess.setServicePlan(servicePlanPostgres);
        postgresAccess.setServiceSourceInstanceName(this.generateServiceName(serviceSourceInstancePostgres));
        postgresAccess.setServiceTargetInstanceName(this.generateServiceName(serviceTargetInstancePostgres));

        DatabaseAccess mongoAccess = this.databaseAccessMap.get(DatabaseType.MONGODB);
        mongoAccess.setServiceName(serviceNameMongo);
        mongoAccess.setServicePlan(servicePlanMongo);
        mongoAccess.setServiceSourceInstanceName(this.generateServiceName(serviceSourceInstanceMongo));
        mongoAccess.setServiceTargetInstanceName(this.generateServiceName(serviceTargetInstanceMongo));

        DatabaseAccess redisAccess = this.databaseAccessMap.get(DatabaseType.REDIS);
        redisAccess.setServiceName(serviceNameRedis);
        redisAccess.setServicePlan(servicePlanRedis);
        String generatedSourceInstanceRedis = this.generateServiceName(serviceSourceInstanceRedis);
        redisAccess.setServiceSourceInstanceName(generatedSourceInstanceRedis);
        if (serviceNameRedis.equals("rediscloud")) {
            redisAccess.setServiceTargetInstanceName(generatedSourceInstanceRedis);
        } else {
            redisAccess.setServiceTargetInstanceName(this.generateServiceName(serviceTargetInstanceRedis));
        }

    }

    protected String generateServiceName(String serviceName) {
        String randomUUID = UUID.randomUUID().toString();
        randomUUID = randomUUID.replace("-", "");
        return serviceName + "-" + randomUUID.substring(0, 5);
    }

    protected void createService(CloudService cloudService) {
        try {
            logger.info("Creating service {} from {} with plan {} ", cloudService.getName(), cloudService.getLabel(), cloudService.getPlan());
            cfClientToPopulate.createService(cloudService);
            if (!this.isFinishedCreatingService(cloudService)) {
                fail("Cannot create service '" + cloudService.getName() + "'");
            }
        } catch (HttpServerErrorException e) {
            if (!e.getStatusCode().equals(HttpStatus.BAD_GATEWAY)) {
                throw e;
            } else {
                assumeTrue("Bad gateway error, skipping test", false);
            }
        }

    }

    public boolean isFinishedCreatingService(CloudService cloudService) {

        ExecutorService executor = Executors.newCachedThreadPool();
        Callable<Boolean> task = () -> {
            while (true) {
                CloudService cloudServiceFound = cfClientToPopulate.getService(cloudService.getName());
                if (cloudServiceFound.getCloudServiceLastOperation() == null) {
                    return true;
                }
                logger.info("State for service '{}' : {}", cloudServiceFound, cloudServiceFound.getCloudServiceLastOperation().getState());
                switch (cloudServiceFound.getCloudServiceLastOperation().getState()) {
                    case "succeeded":
                        return true;
                    case "in progress":
                        break;
                    case "failed":
                    case "internal error":
                        return false;
                }
                Thread.sleep(5000L);// we yield the task for 5seconds to let the service do is work (actually, Cloud Controller hit getServiceInstance every 30sec)
            }
        };
        Future<Boolean> future = executor.submit(task);
        try {
            Boolean result = future.get(timeoutCreatingService, TimeUnit.MINUTES);
            return result;
        } catch (Exception ex) {
            ex.printStackTrace();
            future.cancel(true);
            fail("Timeout reached.", ex);
        }
        return false;
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
