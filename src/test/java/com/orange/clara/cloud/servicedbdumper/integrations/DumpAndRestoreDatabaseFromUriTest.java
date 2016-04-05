package com.orange.clara.cloud.servicedbdumper.integrations;

import com.orange.clara.cloud.servicedbdumper.Application;
import com.orange.clara.cloud.servicedbdumper.model.DatabaseType;
import org.junit.runner.RunWith;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.boot.test.WebIntegrationTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * Copyright (C) 2016 Orange
 * <p>
 * This software is distributed under the terms and conditions of the 'Apache-2.0'
 * license which can be found in the file 'LICENSE' in this package distribution
 * or at 'https://opensource.org/licenses/Apache-2.0'.
 * <p>
 * Author: Arthur Halet
 * Date: 25/03/2016
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(Application.class)
@WebIntegrationTest(randomPort = true)
@ActiveProfiles("local")
public class DumpAndRestoreDatabaseFromUriTest extends AbstractIntegrationTest {


    @Override
    public String getDbParamsForDump(DatabaseType databaseType) {
        switch (databaseType) {
            case MONGODB:
                return this.getDbSourceFromUri(mongoServer);
            case MYSQL:
                return this.getDbSourceFromUri(mysqlServer);
            case POSTGRESQL:
                return this.getDbSourceFromUri(postgresServer);
            case REDIS:
                return redisServer;
        }
        return "";
    }

    @Override
    public String getDbParamsForRestore(DatabaseType databaseType) {
        switch (databaseType) {
            case MONGODB:
                return this.getDbTargetFromUri(mongoServer);
            case MYSQL:
                return this.getDbTargetFromUri(mysqlServer);
            case POSTGRESQL:
                return this.getDbTargetFromUri(postgresServer);
            case REDIS:
                return redisServer;
        }
        return "";
    }

    public String getDbFromUri(String databaseServerUri, String databaseName) {
        int lastPos = databaseServerUri.lastIndexOf('/');
        return databaseServerUri.substring(0, lastPos) + "/" + databaseName;
    }

    public String getDbSourceFromUri(String databaseServerUri) {
        return this.getDbFromUri(databaseServerUri, DATABASE_SOURCE_NAME);
    }

    public String getDbTargetFromUri(String databaseServerUri) {
        return this.getDbFromUri(databaseServerUri, DATABASE_TARGET_NAME);
    }
}
