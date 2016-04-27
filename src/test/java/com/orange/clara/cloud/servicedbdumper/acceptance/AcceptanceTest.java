package com.orange.clara.cloud.servicedbdumper.acceptance;

import com.orange.clara.cloud.servicedbdumper.Application;
import com.orange.clara.cloud.servicedbdumper.exception.CannotFindDatabaseDumperException;
import com.orange.clara.cloud.servicedbdumper.exception.DatabaseExtractionException;
import com.orange.clara.cloud.servicedbdumper.helper.ByteFormat;
import com.orange.clara.cloud.servicedbdumper.integrations.AbstractIntegrationWithRealCfClientTest;
import com.orange.clara.cloud.servicedbdumper.model.DatabaseRef;
import com.orange.clara.cloud.servicedbdumper.model.DatabaseType;
import org.junit.Before;
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
@ActiveProfiles({"local", "integrationrealcf", "s3"})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@IfProfileValue(name = "test.groups", values = {"acceptance-tests"})
public class AcceptanceTest extends AbstractIntegrationWithRealCfClientTest {

    private final static String fileNameTemplate = "fakedata_%s.sql";
    @Value("${accept.cf.service.name.mysql:cleardb}")
    protected String serviceNameAcceptMysql;
    @Value("${accept.cf.service.plan.mysql:spark}")
    protected String servicePlanAcceptMysql;
    @Value("${accept.cf.service.instance.source.mysql:mysql-db-dumper-src-int}")
    protected String serviceSourceInstanceAcceptMysql;
    @Value("${accept.cf.service.instance.target.mysql:mysql-db-dumper-dest-int}")
    protected String serviceTargetInstanceAcceptMysql;
    @Value("${accept.cf.service.name.postgresql:elephantsql}")
    protected String serviceNameAcceptPostgres;
    @Value("${accept.cf.service.plan.postgresql:turtle}")
    protected String servicePlanAcceptPostgres;
    @Value("${accept.cf.service.instance.source.postgresql:postgres-db-dumper-src-int}")
    protected String serviceSourceInstanceAcceptPostgres;
    @Value("${accept.cf.service.instance.target.postgresql:postgres-db-dumper-dest-int}")
    protected String serviceTargetInstanceAcceptPostgres;
    @Value("${accept.cf.service.name.mongodb:mongolab}")
    protected String serviceNameAcceptMongo;
    @Value("${accept.cf.service.plan.mongodb:sandbox}")
    protected String servicePlanAcceptMongo;
    @Value("${accept.cf.service.instance.source.mongodb:mongodb-db-dumper-src-int}")
    protected String serviceSourceInstanceAcceptMongo;
    @Value("${accept.cf.service.instance.target.mongodb:mongodb-db-dumper-dest-int}")
    protected String serviceTargetInstanceAcceptMongo;
    @Value("${accept.cf.service.name.redis:rediscloud}")
    protected String serviceNameAcceptRedis;
    @Value("${accept.cf.service.plan.redis:30mb}")
    protected String servicePlanAcceptRedis;
    @Value("${accept.cf.service.instance.source.redis:redis-db-dumper-src-int}")
    protected String serviceSourceInstanceAcceptRedis;
    @Value("${accept.cf.service.instance.target.redis:redis-db-dumper-dest-int}")
    protected String serviceTargetInstanceAcceptRedis;
    @Value("${user.dir}/bin/create_fake_data")
    private File scriptCreateFakeData;
    @Value("${user.dir}")
    private File userDir;

    @Value("${test.accept.file.size:#{null}}")
    private String fileSize;


    @Override
    @Before
    public void init() throws DatabaseExtractionException {
        assumeTrue("You must set property test.accept.file.size (e.g. test.accept.file.size=100mb",
                this.fileSize != null);
        this.serviceNameMongo = this.serviceNameAcceptMongo;
        this.serviceNameMysql = this.serviceNameAcceptMysql;
        this.serviceNameRedis = this.serviceNameAcceptRedis;
        this.serviceNamePostgres = this.serviceNameAcceptPostgres;
        this.servicePlanMongo = this.servicePlanAcceptMongo;
        this.servicePlanMysql = this.servicePlanAcceptMysql;
        this.servicePlanPostgres = this.servicePlanAcceptPostgres;
        this.servicePlanRedis = this.servicePlanAcceptRedis;
        this.serviceSourceInstanceMongo = serviceSourceInstanceAcceptMongo;
        this.serviceSourceInstanceMysql = serviceSourceInstanceAcceptMysql;
        this.serviceSourceInstancePostgres = serviceSourceInstanceAcceptPostgres;
        this.serviceSourceInstanceRedis = serviceSourceInstanceAcceptRedis;
        this.serviceTargetInstanceMongo = serviceTargetInstanceAcceptMongo;
        this.serviceTargetInstanceMysql = serviceTargetInstanceAcceptMysql;
        this.serviceTargetInstancePostgres = serviceTargetInstanceAcceptPostgres;
        this.serviceTargetInstanceRedis = serviceTargetInstanceAcceptRedis;
        super.init();
    }

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
        return this.databaseAccessMap.get(databaseType).getServiceSourceInstanceName();

    }

    @Override
    public String getDbParamsForRestore(DatabaseType databaseType) {
        return this.databaseAccessMap.get(databaseType).getServiceTargetInstanceName();
    }

    @Override
    public void populateDataToDatabaseRefFromFile(File fakeData, DatabaseRef databaseServer) throws CannotFindDatabaseDumperException, IOException, InterruptedException {
        Long size = (long) (ByteFormat.parse(this.fileSize) * 1.20);
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
