package com.orange.clara.cloud.servicedbdumper.integrations;

import com.orange.clara.cloud.servicedbdumper.Application;
import com.orange.clara.cloud.servicedbdumper.fake.cloudfoundry.CloudFoundryClientFake;
import com.orange.clara.cloud.servicedbdumper.integrations.config.FakeCloudFoundryClientConfig;
import com.orange.clara.cloud.servicedbdumper.model.DatabaseType;
import org.cloudfoundry.client.lib.CloudFoundryClient;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.boot.test.WebIntegrationTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.fest.assertions.Assertions.assertThat;

/**
 * Copyright (C) 2016 Orange
 * <p>
 * This software is distributed under the terms and conditions of the 'Apache-2.0'
 * license which can be found in the file 'LICENSE' in this package distribution
 * or at 'https://opensource.org/licenses/Apache-2.0'.
 * <p>
 * Author: Arthur Halet
 * Date: 06/04/2016
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration({Application.class, FakeCloudFoundryClientConfig.class})
@WebIntegrationTest(randomPort = true)
@ActiveProfiles({"local", "cloud", "integration", "integration-fake-cf-client"})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class DumpAndRestoreDatabaseFromServiceNameWithFakeCloudFoundryClientIT extends AbstractIntegrationTest {

    @Autowired
    @Qualifier("cloudFoundryClientAsAdmin")
    protected CloudFoundryClient cloudFoundryClient;

    @Override
    public String getDbParamsForDump(DatabaseType databaseType) {
        return this.databaseAccessMap.get(databaseType).getDatabaseSourceUri();
    }

    @Override
    public String getDbParamsForRestore(DatabaseType databaseType) {
        assertThat(cloudFoundryClient).isInstanceOf(CloudFoundryClientFake.class);
        CloudFoundryClientFake cloudFoundryClientFake = (CloudFoundryClientFake) cloudFoundryClient;
        cloudFoundryClientFake.setDatabaseUri(this.databaseAccessMap.get(databaseType).getDatabaseTargetUri());
        return databaseType.toString().toLowerCase() + "-myservice-target";
    }
}
