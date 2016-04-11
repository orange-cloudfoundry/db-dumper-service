package com.orange.clara.cloud.servicedbdumper.acceptance;

import com.orange.clara.cloud.servicedbdumper.Application;
import com.orange.clara.cloud.servicedbdumper.exception.CannotFindDatabaseDumperException;
import com.orange.clara.cloud.servicedbdumper.exception.DatabaseExtractionException;
import com.orange.clara.cloud.servicedbdumper.helper.ByteFormat;
import com.orange.clara.cloud.servicedbdumper.integrations.AbstractIntegrationTest;
import com.orange.clara.cloud.servicedbdumper.model.DatabaseRef;
import com.orange.clara.cloud.servicedbdumper.model.DatabaseType;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.boot.test.WebIntegrationTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.IfProfileValue;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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
@IfProfileValue(name = "test.groups", values = {"acceptance-tests"})
public class Accept100MBFileTest extends AbstractIntegrationTest {

    private final static String fileNameTemplate = "fakedata_%s.sql";
    @Value("${user.dir}/bin/create_fake_data")
    private File scriptCreateFakeData;
    @Value("${user.dir}")
    private File userDir;

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

    @Override
    public void populateDataToDatabaseRefFromFile(File fakeData, DatabaseRef databaseServer) throws CannotFindDatabaseDumperException, IOException, InterruptedException {
        Long size = (long) (ByteFormat.parse("100mb") * 1.10);
        File fakeDataGenerated = new File(this.userDir.getAbsolutePath() + "/" + String.format(this.fileNameTemplate, size.toString()));
        String[] command = new String[]{
                this.scriptCreateFakeData.getAbsolutePath(),
                size.toString(),
                fakeDataGenerated.getAbsolutePath()
        };
        List<String[]> commands = new ArrayList<>();
        commands.add(command);
        this.runCommands(commands);
        super.populateDataToDatabaseRefFromFile(fakeDataGenerated, databaseServer);
    }


}
