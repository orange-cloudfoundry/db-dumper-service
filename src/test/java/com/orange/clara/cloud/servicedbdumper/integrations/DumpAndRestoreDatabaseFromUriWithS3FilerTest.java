package com.orange.clara.cloud.servicedbdumper.integrations;

import com.orange.clara.cloud.servicedbdumper.Application;
import com.orange.clara.cloud.servicedbdumper.exception.CannotFindDatabaseDumperException;
import com.orange.clara.cloud.servicedbdumper.exception.DatabaseExtractionException;
import com.orange.clara.cloud.servicedbdumper.model.DatabaseType;
import org.junit.runner.RunWith;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.boot.test.WebIntegrationTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.IOException;

import static org.junit.Assume.assumeTrue;

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
@ActiveProfiles({"local", "integration", "s3"})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class DumpAndRestoreDatabaseFromUriWithS3FilerTest extends AbstractIntegrationTest {

    @Override
    public void doBeforeTest(DatabaseType databaseType) throws DatabaseExtractionException, CannotFindDatabaseDumperException, InterruptedException, IOException {
        boolean isS3urlExists = System.getenv("S3_URL") != null && System.getenv("DYNO") != null;
        if (!isS3urlExists) {
            this.skipCleaning = true;
        }
        assumeTrue("No s3 server found, please set env var S3_URL and DYNO=true", isS3urlExists);
        super.doBeforeTest(databaseType);
    }

    @Override
    public String getDbParamsForDump(DatabaseType databaseType) {
        return this.databaseAccessMap.get(databaseType).getDatabaseSourceUri();
    }

    @Override
    public String getDbParamsForRestore(DatabaseType databaseType) {
        return this.databaseAccessMap.get(databaseType).getDatabaseTargetUri();
    }


}
