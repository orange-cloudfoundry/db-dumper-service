package com.orange.clara.cloud.servicedbdumper.integrations;

import com.google.common.collect.Lists;
import com.google.common.io.ByteStreams;
import com.google.common.io.Files;
import com.orange.clara.cloud.servicedbdumper.dbdumper.DatabaseRefManager;
import com.orange.clara.cloud.servicedbdumper.dbdumper.core.dbdrivers.DatabaseDriver;
import com.orange.clara.cloud.servicedbdumper.dbdumper.core.dbdrivers.DbDumpersFactory;
import com.orange.clara.cloud.servicedbdumper.exception.CannotFindDatabaseDumperException;
import com.orange.clara.cloud.servicedbdumper.exception.DatabaseExtractionException;
import com.orange.clara.cloud.servicedbdumper.exception.ServiceKeyException;
import com.orange.clara.cloud.servicedbdumper.filer.Filer;
import com.orange.clara.cloud.servicedbdumper.model.DatabaseRef;
import com.orange.clara.cloud.servicedbdumper.model.DatabaseType;
import org.cloudfoundry.community.servicebroker.exception.*;
import org.cloudfoundry.community.servicebroker.model.ServiceInstance;
import org.cloudfoundry.community.servicebroker.model.ServiceInstanceLastOperation;
import org.cloudfoundry.community.servicebroker.service.ServiceInstanceService;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;

import java.io.*;
import java.net.Socket;
import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.*;

import static org.fest.assertions.Assertions.assertThat;
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
 * Date: 25/03/2016
 */
abstract public class AbstractIntegrationTest {
    protected final static String DATABASE_SOURCE_NAME = "dbdumpertestsource";
    protected final static String DATABASE_TARGET_NAME = "dbdumpertesttarget";
    protected Logger logger = LoggerFactory.getLogger(AbstractIntegrationTest.class);

    @Autowired
    protected ServiceBrokerRequestForge requestForge;
    @Autowired
    protected DbDumpersFactory dbDumpersFactory;

    @Autowired
    protected ServiceInstanceService dbDumperServiceInstanceService;

    @Value("${int.mysql.server:mysql://root@localhost/dbdumpertestsource}")
    protected String mysqlServer;

    @Value("${int.postgres.server:postgres://postgres@localhost/dbdumpertestsource}")
    protected String postgresServer;

    @Value("${int.redis.server:redis://localhost}")
    protected String redisServer;

    @Value("${int.mongodb.server:mongodb://localhost/dbdumpertestsource}")
    protected String mongoServer;

    @Autowired
    protected Filer filer;

    @Autowired
    protected DatabaseRefManager databaseRefManager;

    @Value("${mongodb.fake.data.file:classpath:data/fake-data-mongodb.bin}")
    private File mongodbFakeData;

    @Value("${mysql.fake.data.file:classpath:data/fake-data-mysql.sql}")
    private File mysqlFakeData;

    @Value("${redis.fake.data.file:classpath:data/fake-data-redis.rdmp}")
    private File redisFakeData;

    @Value("${postgres.fake.data.file:classpath:data/fake-data-postgres.sql}")
    private File postgresFakeData;

    @Autowired
    @Qualifier("postgresBinaryRestore")
    private File psqlBinary;

    @Autowired
    @Qualifier("mysqlBinaryRestore")
    private File mysqlBinary;

    public void doBeforeTest(DatabaseType databaseType) throws DatabaseExtractionException, CannotFindDatabaseDumperException, InterruptedException, IOException {
        assumeTrue(String.format("Server '%s' not accessible, skipping test.", this.getDatabaseServerTest(databaseType)), serverListening(databaseType));
        this.populateData(databaseType);
    }

    public void cleanAfterTest(DatabaseType databaseType) throws DatabaseExtractionException, CannotFindDatabaseDumperException, InterruptedException, IOException {
        DatabaseRef databaseServer = this.getDatabaseRefServerTest(databaseType);
        if (databaseServer == null) {
            fail("Cannot find server for database: " + databaseType);
            return;
        }
        this.dropDatabase(databaseServer);
    }

    abstract public String getDbParamsForDump(DatabaseType databaseType);

    abstract public String getDbParamsForRestore(DatabaseType databaseType);

    protected void dumpAndRestoreTest(DatabaseType databaseType) throws InterruptedException, CannotFindDatabaseDumperException, DatabaseExtractionException, IOException, ServiceBrokerException, ServiceInstanceExistsException, ServiceBrokerAsyncRequiredException, ServiceInstanceUpdateNotSupportedException, ServiceInstanceDoesNotExistException, ServiceKeyException {
        String serviceIdSource = databaseType.toString() + "-service-source";
        String serviceIdTarget = databaseType.toString() + "-service-target";
        this.doBeforeTest(databaseType);
        this.dbDumperServiceInstanceService.createServiceInstance(this.requestForge.createNewDumpRequest(this.getDbParamsForDump(databaseType), serviceIdSource));
        if (!this.isFinishedAction(serviceIdSource)) {
            fail("Creating dump for source database failed");
        }
        this.dbDumperServiceInstanceService.updateServiceInstance(this.requestForge.createRestoreRequest(this.getDbParamsForRestore(databaseType), serviceIdSource));
        if (!this.isFinishedAction(serviceIdSource)) {
            fail("Restoring dump failed");
        }

        this.dbDumperServiceInstanceService.createServiceInstance(this.requestForge.createNewDumpRequest(this.getDbParamsForRestore(databaseType), serviceIdTarget));
        if (!this.isFinishedAction(serviceIdTarget)) {
            fail("Creating dump for target database failed");
        }
        this.diffSourceAndTargetDatabase(databaseType);
        this.cleanAfterTest(databaseType);
        this.dbDumperServiceInstanceService.deleteServiceInstance(this.requestForge.createDeleteServiceRequest(serviceIdSource));
        this.dbDumperServiceInstanceService.deleteServiceInstance(this.requestForge.createDeleteServiceRequest(serviceIdTarget));
    }

    @Test
    public void when_dump_and_restore_a_MYSQL_database_it_should_have_the_database_source_equals_to_the_database_target() throws DatabaseExtractionException, CannotFindDatabaseDumperException, InterruptedException, IOException, ServiceInstanceUpdateNotSupportedException, ServiceBrokerAsyncRequiredException, ServiceBrokerException, ServiceInstanceDoesNotExistException, ServiceKeyException, ServiceInstanceExistsException {
        DatabaseType databaseType = DatabaseType.MYSQL;
        this.dumpAndRestoreTest(databaseType);
    }

    @Test
    public void when_dump_and_restore_a_POSTGRES_database_it_should_have_the_database_source_equals_to_the_database_target() throws DatabaseExtractionException, CannotFindDatabaseDumperException, InterruptedException, IOException, ServiceBrokerException, ServiceInstanceExistsException, ServiceBrokerAsyncRequiredException, ServiceInstanceUpdateNotSupportedException, ServiceInstanceDoesNotExistException, ServiceKeyException {
        DatabaseType databaseType = DatabaseType.POSTGRESQL;
        this.dumpAndRestoreTest(databaseType);
    }

    @Test
    public void when_dump_and_restore_a_REDIS_database_it_should_have_the_database_source_equals_to_the_database_target() throws DatabaseExtractionException, CannotFindDatabaseDumperException, InterruptedException, IOException, ServiceInstanceUpdateNotSupportedException, ServiceBrokerAsyncRequiredException, ServiceBrokerException, ServiceInstanceDoesNotExistException, ServiceKeyException, ServiceInstanceExistsException {
        DatabaseType databaseType = DatabaseType.REDIS;
        this.dumpAndRestoreTest(databaseType);
    }

    @Test
    public void when_dump_and_restore_a_MONGODB_database_it_should_have_the_database_source_equals_to_the_database_target() throws DatabaseExtractionException, CannotFindDatabaseDumperException, InterruptedException, IOException, ServiceInstanceUpdateNotSupportedException, ServiceBrokerAsyncRequiredException, ServiceBrokerException, ServiceInstanceDoesNotExistException, ServiceKeyException, ServiceInstanceExistsException {
        DatabaseType databaseType = DatabaseType.MONGODB;
        this.dumpAndRestoreTest(databaseType);
    }


    public void diffSourceAndTargetDatabase(DatabaseType databaseType) throws DatabaseExtractionException, ServiceKeyException, IOException {
        String databaseSource = this.getDbParamsForDump(databaseType);
        String databaseTarget = this.getDbParamsForRestore(databaseType);
        DatabaseRef sourceDatabase = this.databaseRefManager.getDatabaseRef(databaseSource, ServiceBrokerRequestForge.USER_TOKEN, ServiceBrokerRequestForge.ORG, ServiceBrokerRequestForge.SPACE);
        DatabaseRef targetDatabase = this.databaseRefManager.getDatabaseRef(databaseTarget, ServiceBrokerRequestForge.USER_TOKEN, ServiceBrokerRequestForge.ORG, ServiceBrokerRequestForge.SPACE);
        InputStream sourceStream = this.filer.retrieveWithStream(sourceDatabase.getName() + "/" + sourceDatabase.getDatabaseDumpFiles().get(0).getFileName());
        InputStream targetStream = this.filer.retrieveWithStream(targetDatabase.getName() + "/" + targetDatabase.getDatabaseDumpFiles().get(0).getFileName());
        assertThat(Arrays.equals(ByteStreams.toByteArray(sourceStream), ByteStreams.toByteArray(targetStream)))
                .overridingErrorMessage(String.format("Dumps files between database source '%s' and database target '%s' diverged.", databaseSource, databaseTarget))
                .isTrue();
    }

    protected boolean serverListening(DatabaseType databaseType) throws DatabaseExtractionException {
        DatabaseRef databaseServer = this.getDatabaseRefServerTest(databaseType);
        if (databaseServer == null) {
            return false;
        }
        Socket s = null;
        try {
            s = new Socket(databaseServer.getHost(), databaseServer.getPort());
            return true;
        } catch (Exception e) {
            return false;
        } finally {
            if (s != null)
                try {
                    s.close();
                } catch (Exception e) {
                }
        }
    }


    public void populateData(DatabaseType databaseType) throws DatabaseExtractionException, CannotFindDatabaseDumperException, IOException, InterruptedException {
        File fakeData = this.getFakeData(databaseType);
        if (fakeData == null) {
            fail("Cannot find file for database: " + databaseType);
            return;
        }
        DatabaseRef databaseServer = this.getDatabaseRefServerTest(databaseType);
        if (databaseServer == null) {
            fail("Cannot find server for database: " + databaseType);
            return;
        }
        logger.info("Populating fake data on server: {} - database {} will be created", this.getDatabaseServerTest(databaseType), DATABASE_SOURCE_NAME);
        this.createDatabase(databaseServer);
        databaseServer.setDatabaseName(DATABASE_SOURCE_NAME);
        DatabaseDriver databaseDriver = dbDumpersFactory.getDatabaseDumper(databaseServer);
        Process process = this.runCommandLine(databaseDriver.getRestoreCommandLine());
        OutputStream outputStream = process.getOutputStream();
        InputStream dumpFileInputStream = Files.asByteSource(fakeData).openStream();
        ByteStreams.copy(dumpFileInputStream, outputStream);
        outputStream.flush();
        dumpFileInputStream.close();
        outputStream.close();
        process.waitFor();
        if (process.exitValue() != 0) {
            throw new InterruptedException("\nError during process (exit code is " + process.exitValue() + "): \n"
                    + this.getInputStreamToStringFromProcess(process.getErrorStream())
                    + "\n" + this.getInputStreamToStringFromProcess(process.getInputStream())
            );
        }
        logger.info("Finished to populate fake data on server: {}", this.getDatabaseServerTest(databaseType));
    }

    protected void dropDatabase(DatabaseRef databaseRef) throws IOException, InterruptedException {
        if (System.getenv("TRAVIS") != null) { //we don't drop databases if we are in travis
            return;
        }
        DatabaseType databaseType = databaseRef.getType();
        if (databaseType.equals(DatabaseType.MONGODB) || databaseType.equals(DatabaseType.REDIS)) {
            return;
        }
        List<String[]> dropDatabaseCommands = Lists.newArrayList();
        if (databaseType.equals(DatabaseType.MYSQL)) {
            dropDatabaseCommands.add(new String[]{
                    this.mysqlBinary.getAbsolutePath(),
                    "--host=" + databaseRef.getHost(),
                    "--port=" + databaseRef.getPort(),
                    "--user=" + databaseRef.getUser(),
                    "--password=" + databaseRef.getPassword(),
                    "-e",
                    "DROP DATABASE IF EXISTS " + DATABASE_SOURCE_NAME
            });
            dropDatabaseCommands.add(new String[]{
                    this.mysqlBinary.getAbsolutePath(),
                    "--host=" + databaseRef.getHost(),
                    "--port=" + databaseRef.getPort(),
                    "--user=" + databaseRef.getUser(),
                    "--password=" + databaseRef.getPassword(),
                    "-e",
                    "DROP DATABASE IF EXISTS " + DATABASE_TARGET_NAME
            });
        }
        if (databaseType.equals(DatabaseType.POSTGRESQL)) {
            dropDatabaseCommands.add(new String[]{
                    this.psqlBinary.getAbsolutePath(),
                    String.format("--dbname=postgresql://%s:%s@%s:%s/%s", databaseRef.getUser(), databaseRef.getPassword(), databaseRef.getHost(), databaseRef.getPort(), databaseRef.getDatabaseName()),
                    "-c",
                    "DROP DATABASE IF EXISTS " + DATABASE_SOURCE_NAME
            });
            dropDatabaseCommands.add(new String[]{
                    this.psqlBinary.getAbsolutePath(),
                    String.format("--dbname=postgresql://%s:%s@%s:%s/%s", databaseRef.getUser(), databaseRef.getPassword(), databaseRef.getHost(), databaseRef.getPort(), databaseRef.getDatabaseName()),
                    "-c",
                    "DROP DATABASE IF EXISTS " + DATABASE_TARGET_NAME
            });
        }
        if (dropDatabaseCommands.size() == 0) {
            return;
        }
        this.runCommands(dropDatabaseCommands);
    }

    protected void createDatabase(DatabaseRef databaseRef) throws IOException, InterruptedException {
        DatabaseType databaseType = databaseRef.getType();
        if (databaseType.equals(DatabaseType.MONGODB) || databaseType.equals(DatabaseType.REDIS)) {
            return;
        }
        List<String[]> createDatabaseCommands = Lists.newArrayList();
        if (databaseType.equals(DatabaseType.MYSQL)) {
            createDatabaseCommands.add(new String[]{
                    this.mysqlBinary.getAbsolutePath(),
                    "--host=" + databaseRef.getHost(),
                    "--port=" + databaseRef.getPort(),
                    "--user=" + databaseRef.getUser(),
                    "--password=" + databaseRef.getPassword(),
                    "-e",
                    "CREATE DATABASE IF NOT EXISTS " + DATABASE_SOURCE_NAME
            });
            createDatabaseCommands.add(new String[]{
                    this.mysqlBinary.getAbsolutePath(),
                    "--host=" + databaseRef.getHost(),
                    "--port=" + databaseRef.getPort(),
                    "--user=" + databaseRef.getUser(),
                    "--password=" + databaseRef.getPassword(),
                    "-e",
                    "CREATE DATABASE IF NOT EXISTS " + DATABASE_TARGET_NAME
            });
        }
        if (databaseType.equals(DatabaseType.POSTGRESQL)) {
            this.dropDatabase(databaseRef);
            if (System.getenv("TRAVIS") == null) {
                createDatabaseCommands.add(new String[]{
                        this.psqlBinary.getAbsolutePath(),
                        String.format("--dbname=postgresql://%s:%s@%s:%s/%s", databaseRef.getUser(), databaseRef.getPassword(), databaseRef.getHost(), databaseRef.getPort(), databaseRef.getDatabaseName()),
                        "-c",
                        "CREATE DATABASE " + DATABASE_SOURCE_NAME
                });
            }
            createDatabaseCommands.add(new String[]{
                    this.psqlBinary.getAbsolutePath(),
                    String.format("--dbname=postgresql://%s:%s@%s:%s/%s", databaseRef.getUser(), databaseRef.getPassword(), databaseRef.getHost(), databaseRef.getPort(), databaseRef.getDatabaseName()),
                    "-c",
                    "CREATE DATABASE " + DATABASE_TARGET_NAME
            });
        }
        if (createDatabaseCommands.size() == 0) {
            return;
        }
        this.runCommands(createDatabaseCommands);

    }

    private void runCommands(List<String[]> commands) throws IOException, InterruptedException {
        for (String[] command : commands) {
            Process process = this.runCommandLine(command);
            process.waitFor();
            if (process.exitValue() != 0) {
                throw new InterruptedException("\nError during process (exit code is " + process.exitValue() + "): \n"
                        + this.getInputStreamToStringFromProcess(process.getErrorStream())
                        + "\n" + this.getInputStreamToStringFromProcess(process.getInputStream())
                );
            }
        }
    }

    private String getInputStreamToStringFromProcess(InputStream inputStream) throws IOException {
        String outputFromProcess = "";
        String line = "";
        if (inputStream == null) {
            return outputFromProcess;
        }
        BufferedReader brOutput = new BufferedReader(new InputStreamReader(inputStream));
        while ((line = brOutput.readLine()) != null) {
            outputFromProcess += line + "\n";
        }
        return outputFromProcess;
    }

    protected Process runCommandLine(String[] commandLine) throws IOException, InterruptedException {
        logger.info("Running command line: " + String.join(" ", commandLine));

        ProcessBuilder pb = new ProcessBuilder(commandLine);
        pb.redirectOutput(ProcessBuilder.Redirect.PIPE);
        Process process = pb.start();
        return process;
    }

    private DatabaseRef getDatabaseRefServerTest(DatabaseType databaseType) throws DatabaseExtractionException {
        String databaseServerUri = this.getDatabaseServerTest(databaseType);
        if (databaseServerUri == null) {
            return null;
        }
        return new DatabaseRef("server-localhost", URI.create(databaseServerUri));
    }

    private String getDatabaseServerTest(DatabaseType databaseType) {
        switch (databaseType) {
            case MONGODB:
                return mongoServer;
            case MYSQL:
                return mysqlServer;
            case POSTGRESQL:
                return postgresServer;
            case REDIS:
                return redisServer;
        }
        return null;
    }

    private File getFakeData(DatabaseType databaseType) {
        switch (databaseType) {
            case MONGODB:
                return mongodbFakeData;
            case MYSQL:
                return mysqlFakeData;
            case POSTGRESQL:
                return postgresFakeData;
            case REDIS:
                return redisFakeData;
        }
        return null;
    }

    public boolean isFinishedAction(String serviceInstanceId) {

        ExecutorService executor = Executors.newCachedThreadPool();
        Callable<Boolean> task = () -> {
            while (true) {
                ServiceInstance serviceInstance = dbDumperServiceInstanceService.getServiceInstance(serviceInstanceId);
                ServiceInstanceLastOperation lastOperation = serviceInstance.getServiceInstanceLastOperation();
                switch (lastOperation.getState()) {
                    case "succeeded":
                        return true;
                    case "in progress":
                        break;
                    case "failed":
                    case "internal error":
                        return false;
                }
                Thread.sleep(5000L);
            }
        };
        Future<Boolean> future = executor.submit(task);
        try {
            Boolean result = future.get(3, TimeUnit.MINUTES);
            return result;
        } catch (Exception ex) {
            future.cancel(true);
            fail(ex.getMessage(), ex);
        }
        return false;
    }
}
