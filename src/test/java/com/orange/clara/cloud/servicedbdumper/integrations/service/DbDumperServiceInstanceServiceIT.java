package com.orange.clara.cloud.servicedbdumper.integrations.service;

import com.orange.clara.cloud.servicedbdumper.Application;
import com.orange.clara.cloud.servicedbdumper.fake.cloudfoundry.CloudFoundryClientFake;
import com.orange.clara.cloud.servicedbdumper.integrations.ServiceBrokerRequestForge;
import com.orange.clara.cloud.servicedbdumper.integrations.config.FakeCloudFoundryClientConfig;
import org.cloudfoundry.community.servicebroker.exception.ServiceBrokerAsyncRequiredException;
import org.cloudfoundry.community.servicebroker.exception.ServiceBrokerException;
import org.cloudfoundry.community.servicebroker.exception.ServiceInstanceExistsException;
import org.cloudfoundry.community.servicebroker.model.CreateServiceInstanceRequest;
import org.cloudfoundry.community.servicebroker.service.ServiceInstanceService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.boot.test.WebIntegrationTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.fest.assertions.Assertions.assertThat;
import static org.junit.Assert.fail;

/**
 * Copyright (C) 2016 Orange
 * <p>
 * This software is distributed under the terms and conditions of the 'Apache-2.0'
 * license which can be found in the file 'LICENSE' in this package distribution
 * or at 'https://opensource.org/licenses/Apache-2.0'.
 * <p>
 * Author: Arthur Halet
 * Date: 16/08/2016
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration({Application.class, FakeCloudFoundryClientConfig.class})
@WebIntegrationTest(randomPort = true)
@ActiveProfiles({"local", "cloud", "integration", "integration-fake-cf-client"})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class DbDumperServiceInstanceServiceIT {

    @Autowired
    protected ServiceInstanceService dbDumperServiceInstanceService;

    @Autowired
    private ServiceBrokerRequestForge serviceBrokerRequestForge;


    @Before
    public void setup() {
        serviceBrokerRequestForge.setOrg(null);
        serviceBrokerRequestForge.setOrgGuid(null);
        serviceBrokerRequestForge.setSpace(null);
        serviceBrokerRequestForge.setSpaceGuid(null);
        serviceBrokerRequestForge.setUserToken(null);
    }

    @Test
    public void when_unsupported_database_type_is_passed_it_should_give_back_a_message_to_say_its_not_supported() throws ServiceInstanceExistsException, ServiceBrokerAsyncRequiredException {
        CreateServiceInstanceRequest instanceRequest = serviceBrokerRequestForge.createNewDumpRequest("hdfs://my.db.com/mydb", "instance-1");
        try {
            this.dbDumperServiceInstanceService.createServiceInstance(instanceRequest);
            fail("It should raise an ServiceBrokerException");
        } catch (ServiceBrokerException e) {
            assertThat(e.getMessage()).contains("The database driver 'hdfs' is not supported");
        }
    }

    @Test
    public void when_a_service_name_passed_not_exists_it_should_give_back_a_message_to_say_it() throws ServiceInstanceExistsException, ServiceBrokerAsyncRequiredException {
        serviceBrokerRequestForge.setOrg("org");
        serviceBrokerRequestForge.setOrgGuid("org-1");
        serviceBrokerRequestForge.setSpace("space");
        serviceBrokerRequestForge.setSpaceGuid("space-1");
        serviceBrokerRequestForge.setUserToken("usertoken");
        CreateServiceInstanceRequest instanceRequest = serviceBrokerRequestForge.createNewDumpRequest(CloudFoundryClientFake.SERVICE_NOT_ACCESSIBLE, "instance-1");
        try {
            this.dbDumperServiceInstanceService.createServiceInstance(instanceRequest);
            fail("It should raise an ServiceBrokerException");
        } catch (ServiceBrokerException e) {
            assertThat(e.getMessage()).contains("User don't have access to service '" + CloudFoundryClientFake.SERVICE_NOT_ACCESSIBLE + "'");
        }
    }

    @Test
    public void when_a_service_name_passed_and_org_or_space_parameter_not_passed_it_should_give_back_a_message_to_say_it() throws Exception {
        serviceBrokerRequestForge.setUserToken("usertoken");
        CreateServiceInstanceRequest instanceRequest = serviceBrokerRequestForge.createNewDumpRequest("instance-exists", "instance-1");
        try {
            this.dbDumperServiceInstanceService.createServiceInstance(instanceRequest);
            fail("It should raise an ServiceBrokerException");
        } catch (ServiceBrokerException e) {
            assertThat(e.getMessage()).contains("Space and org parameters can't be empty.");
        }
        serviceBrokerRequestForge.setOrg("org");
        serviceBrokerRequestForge.setOrgGuid("org-1");
        instanceRequest = serviceBrokerRequestForge.createNewDumpRequest("instance-exists", "instance-1");
        try {
            this.dbDumperServiceInstanceService.createServiceInstance(instanceRequest);
            fail("It should raise an ServiceBrokerException");
        } catch (ServiceBrokerException e) {
            assertThat(e.getMessage()).contains("Space and org parameters can't be empty.");
        }
        serviceBrokerRequestForge.setOrg(null);
        serviceBrokerRequestForge.setOrgGuid(null);
        serviceBrokerRequestForge.setSpace("space");
        serviceBrokerRequestForge.setSpaceGuid("space-1");
        instanceRequest = serviceBrokerRequestForge.createNewDumpRequest("instance-exists", "instance-1");
        try {
            this.dbDumperServiceInstanceService.createServiceInstance(instanceRequest);
            fail("It should raise an ServiceBrokerException");
        } catch (ServiceBrokerException e) {
            assertThat(e.getMessage()).contains("Space and org parameters can't be empty.");
        }
    }
}
