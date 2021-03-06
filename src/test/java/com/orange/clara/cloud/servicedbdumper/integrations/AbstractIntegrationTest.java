package com.orange.clara.cloud.servicedbdumper.integrations;

import com.google.common.collect.Maps;
import com.google.common.io.ByteStreams;
import com.google.common.io.Files;
import com.orange.clara.cloud.servicedbdumper.dbdumper.DatabaseRefManager;
import com.orange.clara.cloud.servicedbdumper.dbdumper.core.dbdrivers.DatabaseDriver;
import com.orange.clara.cloud.servicedbdumper.dbdumper.core.dbdrivers.DbDumpersFactory;
import com.orange.clara.cloud.servicedbdumper.exception.CannotFindDatabaseDumperException;
import com.orange.clara.cloud.servicedbdumper.exception.DatabaseExtractionException;
import com.orange.clara.cloud.servicedbdumper.exception.ServiceKeyException;
import com.orange.clara.cloud.servicedbdumper.filer.Filer;
import com.orange.clara.cloud.servicedbdumper.integrations.model.DatabaseAccess;
import com.orange.clara.cloud.servicedbdumper.model.*;
import com.orange.clara.cloud.servicedbdumper.repo.DatabaseDumpFileRepo;
import com.orange.clara.cloud.servicedbdumper.repo.DbDumperServiceInstanceRepo;
import com.orange.clara.cloud.servicedbdumper.service.DbDumperServiceInstanceBindingService;
import com.orange.clara.cloud.servicedbdumper.utiltest.ReportIntegration;
import com.orange.clara.cloud.servicedbdumper.utiltest.ReportManager;
import org.cloudfoundry.community.servicebroker.exception.*;
import org.cloudfoundry.community.servicebroker.model.ServiceInstance;
import org.cloudfoundry.community.servicebroker.model.ServiceInstanceBinding;
import org.cloudfoundry.community.servicebroker.model.ServiceInstanceLastOperation;
import org.cloudfoundry.community.servicebroker.service.ServiceInstanceBindingService;
import org.cloudfoundry.community.servicebroker.service.ServiceInstanceService;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;

import java.io.*;
import java.net.Socket;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
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
    protected final static String BINDING_ID = "db-dumper-service-binding";
    protected ReportIntegration reportIntegration;
    protected Map<DatabaseType, DatabaseAccess> databaseAccessMap = Maps.newHashMap();

    protected Logger logger = LoggerFactory.getLogger(AbstractIntegrationTest.class);
    @Autowired
    protected ServiceBrokerRequestForge requestForge;
    @Autowired
    protected DbDumpersFactory dbDumpersFactory;

    @Autowired
    protected ServiceInstanceService dbDumperServiceInstanceService;

    @Autowired
    protected ServiceInstanceBindingService serviceInstanceBindingService;

    @Value("${int.mysql.server:mysql://root@localhost/dbdumpertestsource}")
    protected String mysqlServer;

    @Value("${int.postgres.server:postgres://postgres@localhost/dbdumpertestsource}")
    protected String postgresServer;

    @Value("${int.redis.server:redis://localhost}")
    protected String redisServer;

    @Value("${int.mongodb.server:mongodb://localhost/dbdumpertestsource}")
    protected String mongoServer;
    @Value("${test.populate.data.retry:5}")
    protected int populateDataRetry;
    @Autowired
    protected Filer filer;

    @Autowired
    protected DatabaseRefManager databaseRefManager;
    @Autowired
    protected DbDumperServiceInstanceRepo serviceInstanceRepo;
    protected DatabaseType currentDatabaseType;
    protected String serviceIdSource;
    protected String bindingId;
    protected String serviceIdTarget;
    protected boolean skipCleaning;
    @Value("${test.timeout.action:3}")
    protected int timeoutAction;
    protected String prefixReportName = "";
    @Value("${mongodb.fake.data.file:classpath:data/fake-data-mongodb.bin}")
    private File mongodbFakeData;
    @Value("${mysql.fake.data.file:classpath:data/fake-data-mysql.sql}")
    private File mysqlFakeData;
    @Value("${redis.fake.data.file:classpath:data/fake-data-redis.rdmp}")
    private File redisFakeData;
    @Value("${postgres.fake.data.file:classpath:data/fake-data-postgres.sql}")
    private File postgresFakeData;
    @Value("${binaries.db.folder:classpath:binaries}")
    private File binariesFolder;
    @Autowired
    @Qualifier("postgresBinaryRestore")
    private File psqlBinary;
    @Value("${int.check.binaries:true}")
    private boolean checkBinariesValid;
    @Autowired
    @Qualifier("mysqlBinaryRestore")
    private File mysqlBinary;
    @Autowired
    private DatabaseDumpFileRepo dumpFileRepo;
    @Value("${test.chunk.size.diff:2097152}")
    private int chunkSizeDiff;

    @Before
    public void init() throws DatabaseExtractionException {
        this.prefixReportName = humanize.Humanize.decamelize(this.getClass().getSimpleName()) + " ";
        skipCleaning = false;
        currentDatabaseType = null;
        bindingId = null;
        serviceIdSource = null;
        serviceIdTarget = null;
        this.requestForge.setMetadata(null);
        this.populateDatabaseAccessMap();
    }

    public void doBeforeTest(DatabaseType databaseType) throws DatabaseExtractionException, CannotFindDatabaseDumperException, InterruptedException, IOException {
        boolean isBinariesValid = this.isBinariesValid(databaseType);
        boolean isServerListening = isServerListening(databaseType);
        String skipMessage = "";
        if (!isBinariesValid) {
            this.skipCleaning = true;
            skipMessage = String.format("Binaries for database %s use for integrations test are not correct, you're not running on linux 64, please set %s.dump.bin.path and %s.restore.bin.path .\nSkipping test.",
                    databaseType.toString().toLowerCase(),
                    databaseType.toString().toLowerCase(),
                    databaseType.toString().toLowerCase()
            );
            this.reportIntegration.setSkipped(true);
            this.reportIntegration.setSkippedReason(skipMessage);
            assumeTrue(skipMessage, false);
        }
        if (!isServerListening) {
            skipMessage = String.format("Server(s) for '%s' not accessible, skipping test.", databaseType.toString().toLowerCase());
            this.skipCleaning = true;
            this.reportIntegration.setSkipped(true);
            this.reportIntegration.setSkippedReason(skipMessage);
            assumeTrue(skipMessage, false);
        }
        this.populateData(databaseType);
    }

    public void cleanDatabase(DatabaseType databaseType) throws DatabaseExtractionException, CannotFindDatabaseDumperException, InterruptedException, IOException {
        this.dropDatabase(databaseType);
    }

    @After
    public void cleanAfterTest() throws DatabaseExtractionException, CannotFindDatabaseDumperException, InterruptedException, IOException, ServiceBrokerAsyncRequiredException, ServiceBrokerException {
        if (this.skipCleaning) {
            return;
        }
        if (bindingId != null) {
            try {
                this.deleteBinding();
            } catch (ServiceBrokerAsyncRequiredException | ServiceBrokerException e) {
            }
        }
        if (serviceIdSource != null && !serviceIdSource.isEmpty()) {
            this.deleteServiceInstance(serviceIdSource);
        }
        if (serviceIdTarget != null && !serviceIdTarget.isEmpty()) {
            this.deleteServiceInstance(serviceIdTarget);
        }
        Iterable<DatabaseDumpFile> databaseDumpFiles = this.dumpFileRepo.findAll();
        for (DatabaseDumpFile databaseDumpFile : databaseDumpFiles) {
            String filePath = databaseDumpFile.getDbDumperServiceInstance().getDatabaseRef().getName() + "/" + databaseDumpFile.getFileName();
            try {
                this.filer.delete(filePath);
                this.dumpFileRepo.delete(databaseDumpFile);
            } catch (Exception e) {

            }

        }
        if (currentDatabaseType == null) {
            return;
        }
        this.cleanDatabase(currentDatabaseType);
    }

    protected void deleteServiceInstance(String instanceId) throws ServiceBrokerAsyncRequiredException, ServiceBrokerException {
        this.dbDumperServiceInstanceService.deleteServiceInstance(this.requestForge.createDeleteServiceRequest(instanceId));
    }

    abstract public String getDbParamsForDump(DatabaseType databaseType);

    abstract public String getDbParamsForRestore(DatabaseType databaseType);

    protected void createSourceDatabaseDump(DatabaseType databaseType) throws ServiceBrokerException, ServiceInstanceExistsException, ServiceBrokerAsyncRequiredException {
        this.dbDumperServiceInstanceService.createServiceInstance(this.requestForge.createNewDumpRequest(this.getDbParamsForDump(databaseType), serviceIdSource));
        if (!this.isFinishedAction(serviceIdSource)) {
            fail("Creating dump for source database failed");
        }
    }

    protected void createSourceDatabaseDumpFromExistingService(DatabaseType databaseType) throws ServiceInstanceDoesNotExistException, ServiceInstanceUpdateNotSupportedException, ServiceBrokerException, ServiceBrokerAsyncRequiredException {
        this.dbDumperServiceInstanceService.updateServiceInstance(this.requestForge.createDumpFromExistingServiceRequest(this.getDbParamsForDump(databaseType), serviceIdSource));
        if (!this.isFinishedAction(serviceIdSource)) {
            fail("Creating new dump for source database failed");
        }
    }

    protected ServiceInstanceBinding getBinding(Map<String, Object> params) throws ServiceInstanceBindingExistsException, ServiceBrokerException {
        this.loadBeforeAction();
        return this.serviceInstanceBindingService.createServiceInstanceBinding(this.requestForge.createBindingCreationRequest(serviceIdSource, bindingId, params));
    }

    protected ServiceInstanceBinding deleteBinding() throws ServiceBrokerAsyncRequiredException, ServiceBrokerException {
        this.loadBeforeAction();
        return this.serviceInstanceBindingService.deleteServiceInstanceBinding(this.requestForge.createBindingDeletionRequest(bindingId));
    }

    protected void createTargetDatabaseDump(DatabaseType databaseType) throws ServiceBrokerException, ServiceInstanceExistsException, ServiceBrokerAsyncRequiredException {
        this.dbDumperServiceInstanceService.createServiceInstance(this.requestForge.createNewDumpRequest(this.getDbParamsForRestore(databaseType), serviceIdTarget));
        if (!this.isFinishedAction(serviceIdTarget)) {
            fail("Creating dump for target database failed");
        }
    }

    protected void restoreSourceDatabaseDump(DatabaseType databaseType) throws ServiceBrokerException, ServiceInstanceExistsException, ServiceBrokerAsyncRequiredException, ServiceInstanceUpdateNotSupportedException, ServiceInstanceDoesNotExistException {
        this.dbDumperServiceInstanceService.updateServiceInstance(this.requestForge.createRestoreRequest(this.getDbParamsForRestore(databaseType), serviceIdSource));
        if (!this.isFinishedAction(serviceIdSource)) {
            fail("Restoring dump failed");
        }
    }

    protected void loadServiceIds(DatabaseType databaseType) {
        serviceIdSource = databaseType.toString() + "-service-source";
        serviceIdTarget = databaseType.toString() + "-service-target";
    }

    protected void loadBindingId() {
        bindingId = BINDING_ID;
    }

    protected void dumpAndRestoreTest(DatabaseType databaseType) throws ServiceBrokerException, InterruptedException, ServiceBrokerAsyncRequiredException, IOException, DatabaseExtractionException, CannotFindDatabaseDumperException, ServiceKeyException, ServiceInstanceExistsException, ServiceInstanceUpdateNotSupportedException, ServiceInstanceDoesNotExistException {
        this.loadServiceIds(databaseType);
        this.currentDatabaseType = databaseType;
        this.doBeforeTest(databaseType);

        this.loadBeforeAction();
        long currentTime = System.currentTimeMillis();
        createSourceDatabaseDump(databaseType);
        this.reportIntegration.setDumpDatabaseSourceTime((System.currentTimeMillis() - currentTime) / 1000);
        logger.info("Dump database source finished after {}", humanize.Humanize.duration(this.reportIntegration.getDumpDatabaseSourceTime()));

        this.loadBeforeAction();
        currentTime = System.currentTimeMillis();
        this.restoreSourceDatabaseDump(databaseType);
        this.reportIntegration.setRestoreDatabaseSourceToTargetTime((System.currentTimeMillis() - currentTime) / 1000);
        logger.info("Restore database source to database target finished after {}", humanize.Humanize.duration(this.reportIntegration.getRestoreDatabaseSourceToTargetTime()));


        this.loadBeforeAction();
        currentTime = System.currentTimeMillis();
        this.createTargetDatabaseDump(databaseType);
        this.reportIntegration.setDumpDatabaseTargetTime((System.currentTimeMillis() - currentTime) / 1000);
        logger.info("Dump database target finished after {}", humanize.Humanize.duration(this.reportIntegration.getDumpDatabaseTargetTime()));
        this.diffSourceAndTargetDatabase(databaseType);
        this.reportIntegration.setFailed(false);
    }

    protected void loadBeforeAction() {

    }

    public boolean isBinariesValid(DatabaseType databaseType) {
        if (!this.checkBinariesValid) {
            return true;
        }

        String OS = System.getProperty("os.name").toLowerCase();
        String arch = System.getProperty("os.arch");
        for (File fileToCheck : this.databaseAccessMap.get(databaseType).getBinaries()) {
            if (fileToCheck.getAbsolutePath().contains(binariesFolder.getAbsolutePath()) && (!OS.contains("nux") || !arch.contains("64"))) {
                return false;
            }
        }
        return true;
    }

    @Test
    public void when_binding_to_a_db_dumper_i_should_have_correct_information_about_my_dumps() throws InterruptedException, CannotFindDatabaseDumperException, DatabaseExtractionException, IOException, ServiceBrokerException, ServiceInstanceExistsException, ServiceBrokerAsyncRequiredException, ServiceInstanceDoesNotExistException, ServiceInstanceUpdateNotSupportedException, ServiceInstanceBindingExistsException {
        this.reportIntegration = ReportManager.createReportIntegration(prefixReportName + " - when binding to a db dumper i should have correct information about my dumps");
        DatabaseType databaseType = DatabaseType.MYSQL;
        Metadata metadata = new Metadata();
        metadata.setTags(Arrays.asList("mytag"));

        this.requestForge.setMetadata(metadata);

        this.loadServiceIds(databaseType);
        this.loadBindingId();

        this.currentDatabaseType = databaseType;
        this.doBeforeTest(databaseType);

        this.loadBeforeAction();
        createSourceDatabaseDump(databaseType);

        this.requestForge.setMetadata(null);
        this.loadBeforeAction();
        createSourceDatabaseDumpFromExistingService(databaseType);

        ServiceInstanceBinding serviceInstanceBinding = getBinding(Maps.newHashMap());
        assertThat(serviceInstanceBinding.getCredentials().get("dumps")).isNotNull();
        assertThat(serviceInstanceBinding.getCredentials().get("dumps")).isInstanceOfAny(List.class);

        List<Map<String, Object>> credentials = (List<Map<String, Object>>) serviceInstanceBinding.getCredentials().get("dumps");
        assertThat(credentials).hasSize(2);

        deleteBinding();

        Map<String, Object> parameters = Maps.newHashMap();
        parameters.put(DbDumperServiceInstanceBindingService.FIND_BY_TAGS_KEY, Arrays.asList("mytag"));

        serviceInstanceBinding = getBinding(parameters);

        assertThat(serviceInstanceBinding.getCredentials().get("dumps")).isNotNull();
        assertThat(serviceInstanceBinding.getCredentials().get("dumps")).isInstanceOfAny(List.class);

        credentials = (List<Map<String, Object>>) serviceInstanceBinding.getCredentials().get("dumps");
        assertThat(credentials).hasSize(1);

        deleteBinding();

        parameters = Maps.newHashMap();
        parameters.put(DbDumperServiceInstanceBindingService.SEE_ALL_DUMPS_KEY, true);

        serviceInstanceBinding = getBinding(parameters);

        assertThat(serviceInstanceBinding.getCredentials().get("dumps")).isNotNull();
        assertThat(serviceInstanceBinding.getCredentials().get("dumps")).isInstanceOfAny(List.class);

        credentials = (List<Map<String, Object>>) serviceInstanceBinding.getCredentials().get("dumps");
        assertThat(credentials).hasSize(2);

        deleteBinding();

        this.reportIntegration.setFailed(false);
    }

    @Test
    public void when_dump_and_restore_a_MYSQL_database_it_should_have_the_database_source_equals_to_the_database_target() throws DatabaseExtractionException, CannotFindDatabaseDumperException, InterruptedException, IOException, ServiceInstanceUpdateNotSupportedException, ServiceBrokerAsyncRequiredException, ServiceBrokerException, ServiceInstanceDoesNotExistException, ServiceKeyException, ServiceInstanceExistsException {
        this.reportIntegration = ReportManager.createReportIntegration(prefixReportName + "MySQL");
        DatabaseType databaseType = DatabaseType.MYSQL;
        this.dumpAndRestoreTest(databaseType);
    }

    @Test
    public void when_dump_and_restore_a_POSTGRES_database_it_should_have_the_database_source_equals_to_the_database_target() throws DatabaseExtractionException, CannotFindDatabaseDumperException, InterruptedException, IOException, ServiceBrokerException, ServiceInstanceExistsException, ServiceBrokerAsyncRequiredException, ServiceInstanceUpdateNotSupportedException, ServiceInstanceDoesNotExistException, ServiceKeyException {
        this.reportIntegration = ReportManager.createReportIntegration(prefixReportName + "PostgreSQL");
        DatabaseType databaseType = DatabaseType.POSTGRESQL;
        this.dumpAndRestoreTest(databaseType);
    }

    @Test
    public void when_dump_and_restore_a_REDIS_database_it_should_have_the_database_source_equals_to_the_database_target() throws DatabaseExtractionException, CannotFindDatabaseDumperException, InterruptedException, IOException, ServiceInstanceUpdateNotSupportedException, ServiceBrokerAsyncRequiredException, ServiceBrokerException, ServiceInstanceDoesNotExistException, ServiceKeyException, ServiceInstanceExistsException {
        this.reportIntegration = ReportManager.createReportIntegration(prefixReportName + "Redis");
        DatabaseType databaseType = DatabaseType.REDIS;
        this.dumpAndRestoreTest(databaseType);
    }

    @Test
    public void when_dump_and_restore_a_MONGODB_database_it_should_have_the_database_source_equals_to_the_database_target() throws DatabaseExtractionException, CannotFindDatabaseDumperException, InterruptedException, IOException, ServiceInstanceUpdateNotSupportedException, ServiceBrokerAsyncRequiredException, ServiceBrokerException, ServiceInstanceDoesNotExistException, ServiceKeyException, ServiceInstanceExistsException {
        this.reportIntegration = ReportManager.createReportIntegration(prefixReportName + "MongoDB");
        DatabaseType databaseType = DatabaseType.MONGODB;
        this.dumpAndRestoreTest(databaseType);
    }

    protected InputStream getDatabaseStream(String serviceId) throws DatabaseExtractionException, ServiceKeyException, IOException {
        this.loadBeforeAction();
        DbDumperServiceInstance dbDumperServiceInstance = this.serviceInstanceRepo.findOne(serviceId);
        DatabaseRef databaseRef = dbDumperServiceInstance.getDatabaseRef();

        assertThat(dbDumperServiceInstance.getDatabaseDumpFiles().size() > 0)
                .overridingErrorMessage(String.format("Database '%s' should have least one dump file.", databaseRef.getName()))
                .isTrue();
        assertThat(dbDumperServiceInstance.getDatabaseDumpFiles().get(0)).isNotNull();
        String fileSource = databaseRef.getName() + "/" + dbDumperServiceInstance.getDatabaseDumpFiles().get(dbDumperServiceInstance.getDatabaseDumpFiles().size() - 1).getFileName();

        this.loadBeforeAction();
        this.databaseRefManager.deleteServiceKey(databaseRef);
        return this.filer.retrieveWithStream(fileSource);
    }

    protected InputStream getSourceStream(DatabaseType databaseType) throws DatabaseExtractionException, ServiceKeyException, IOException {
        return this.getDatabaseStream(serviceIdSource);
    }

    protected InputStream getTargetStream(DatabaseType databaseType) throws DatabaseExtractionException, ServiceKeyException, IOException {
        return this.getDatabaseStream(serviceIdTarget);
    }

    public void diffSourceAndTargetDatabase(DatabaseType databaseType) throws DatabaseExtractionException, ServiceKeyException, IOException {
        long currentTime = System.currentTimeMillis();
        InputStream sourceStream = this.getSourceStream(databaseType);
        InputStream targetStream = this.getTargetStream(databaseType);


        int sourceRead = 0;
        int targetRead = 0;
        Long sourceNumberBytesRead = 0L;
        Long targetNumberBytesRead = 0L;
        boolean shouldContinue = true;
        // we create chunk of data to avoid memory heap space
        while (shouldContinue) {
            byte[] sourceBytes = new byte[this.chunkSizeDiff];
            byte[] targetBytes = new byte[this.chunkSizeDiff];
            sourceRead = ByteStreams.read(sourceStream, sourceBytes, 0, sourceBytes.length);
            sourceNumberBytesRead += sourceRead;
            if (sourceRead != sourceBytes.length) {
                shouldContinue = false;
                sourceBytes = Arrays.copyOf(sourceBytes, sourceRead);
                if (sourceBytes.length == 0) {
                    break;
                }
            }
            targetRead = ByteStreams.read(targetStream, targetBytes, 0, targetBytes.length);
            targetNumberBytesRead += targetRead;
            if (targetRead != targetBytes.length) {
                shouldContinue = false;
                targetBytes = Arrays.copyOf(targetBytes, targetRead);
                if (targetBytes.length == 0) {
                    break;
                }
            }
            if (databaseType.equals(DatabaseType.REDIS)) { // Redis rearrange data which make diff files unreliable
                continue;
            }
            if (dbDumpersFactory.getDatabaseDumper(databaseType).isDumpShowable()) {
                assertThat(new String(targetBytes)).isEqualTo(new String(sourceBytes));
            } else {
                assertThat(Arrays.equals(targetBytes, sourceBytes))
                        .overridingErrorMessage(String.format("Dumps files between database source and database target diverged."))
                        .isTrue();
            }
        }
        assertThat(sourceNumberBytesRead).isEqualTo(targetNumberBytesRead);
        this.reportIntegration.setDiffTime((System.currentTimeMillis() - currentTime) / 1000);
        this.logger.info("Diff against source and target database finished after {}", humanize.Humanize.duration(this.reportIntegration.getDiffTime()));
    }

    protected boolean isServerListening(DatabaseType databaseType) throws DatabaseExtractionException {
        DatabaseRef databaseServer = this.databaseAccessMap.get(databaseType).generateDatabaseRef();
        if (databaseServer == null) {
            return false;
        }
        return this.isSocketOpen(databaseServer.getHost(), databaseServer.getPort());
    }

    protected boolean isSocketOpen(String host, Integer port) {
        Socket s = null;
        try {
            s = new Socket(host, port);
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
        File fakeData = this.databaseAccessMap.get(databaseType).getFakeDataFile();
        if (fakeData == null) {
            fail("Cannot find file for database: " + databaseType);
            return;
        }
        DatabaseRef databaseServer = this.databaseAccessMap.get(databaseType).generateDatabaseRef();
        if (databaseServer == null) {
            fail("Cannot find server for database: " + databaseType);
            return;
        }

        this.createDatabase(databaseType);
        databaseServer.setDatabaseName(DATABASE_SOURCE_NAME);
        this.populateDataToDatabaseRefFromFile(fakeData, databaseServer);
    }

    public void populateDataToDatabaseRefFromFile(File fakeData, DatabaseRef databaseServer) throws CannotFindDatabaseDumperException, IOException, InterruptedException {
        this.reportIntegration.setFakeDataFileSize(fakeData.length());
        long currentTime = System.currentTimeMillis();
        logger.info("Populating fake data on server: {} - database {} will be created with data from file {} which has size of {}", databaseServer.getHost(), DATABASE_SOURCE_NAME, fakeData.getAbsolutePath(), humanize.Humanize.binaryPrefix(fakeData.length()));
        DatabaseDriver databaseDriver = dbDumpersFactory.getDatabaseDumper(databaseServer);
        String[] restoreCommandLine = databaseDriver.getRestoreCommandLine();
        int i = 1;
        while (true) {
            Process process = this.runCommandLine(restoreCommandLine);
            OutputStream outputStream = process.getOutputStream();
            InputStream dumpFileInputStream = Files.asByteSource(fakeData).openStream();
            try {
                ByteStreams.copy(dumpFileInputStream, outputStream);
                outputStream.flush();
                outputStream.close();
            } catch (IOException e) {

            } finally {
                dumpFileInputStream.close();
            }
            process.waitFor();
            if (process.exitValue() == 0) {
                break;
            }
            dumpFileInputStream.close();
            outputStream.close();
            if (i >= populateDataRetry) {
                throw this.generateInterruptedExceptionFromProcess(process);
            }
            logger.warn("Retry {}/{}: fail to populate data.", i, populateDataRetry);
            i++;
        }
        this.reportIntegration.setPopulateToDatabaseTime((System.currentTimeMillis() - currentTime) / 1000);
        logger.info("Finished to populate fake data on server: {} \n Duration: {}", databaseServer.getHost(), humanize.Humanize.duration(this.reportIntegration.getPopulateToDatabaseTime()));
    }

    protected void dropDatabase(DatabaseType databaseType) throws IOException, InterruptedException {
        if (System.getenv("TRAVIS") != null) { //we don't drop databases if we are in travis
            return;
        }

        List<String[]> dropDatabaseCommands = this.databaseAccessMap.get(databaseType).getDropDatabaseCommands();

        if (dropDatabaseCommands.size() == 0) {
            return;
        }
        this.runCommands(dropDatabaseCommands);
    }

    protected void createDatabase(DatabaseType databaseType) throws IOException, InterruptedException {
        if (System.getenv("TRAVIS") != null) { // if in travis we don't try to create database cause they already exists
            return;
        }
        List<String[]> createDatabaseCommands = this.databaseAccessMap.get(databaseType).getCreateDatabaseCommands();

        if (databaseType.equals(DatabaseType.POSTGRESQL)) {
            this.dropDatabase(databaseType);
        }
        if (createDatabaseCommands.size() == 0) {
            return;
        }
        this.runCommands(createDatabaseCommands);

    }

    protected void runCommands(List<String[]> commands) throws IOException, InterruptedException {
        for (String[] command : commands) {
            this.runCommand(command);
        }
    }

    protected InterruptedException generateInterruptedExceptionFromProcess(Process process) throws IOException {
        return new InterruptedException("\nError during process (exit code is " + process.exitValue() + "): \n"
                + this.getInputStreamToStringFromProcess(process.getErrorStream())
                + "\n" + this.getInputStreamToStringFromProcess(process.getInputStream())
        );
    }

    protected void runCommand(String[] command) throws IOException, InterruptedException {
        this.runCommand(command, false);
    }

    protected void runCommand(String[] command, boolean showStdout) throws IOException, InterruptedException {
        Process process = null;
        if (showStdout) {
            process = this.runCommandLineWithStdoutShowed(command);
        } else {
            process = this.runCommandLine(command);
        }
        process.waitFor();
        if (process.exitValue() != 0) {
            throw this.generateInterruptedExceptionFromProcess(process);
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

    protected Process runCommandLineWithStdoutShowed(String[] commandLine) throws IOException, InterruptedException {
        logger.info("Running command line: " + String.join(" ", commandLine));

        ProcessBuilder pb = new ProcessBuilder(commandLine);
        pb.inheritIO();
        Process process = pb.start();
        return process;
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
                Thread.sleep(5000L);// we yield the task for 5seconds to let the service do is work (actually, Cloud Controller hit getServiceInstance every 30sec)
            }
        };
        Future<Boolean> future = executor.submit(task);
        try {
            Boolean result = future.get(timeoutAction, TimeUnit.MINUTES);
            return result;
        } catch (Exception ex) {
            future.cancel(true);
            fail("Timeout reached.", ex);
        }
        return false;
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

    protected void populateDatabaseAccessMap() throws DatabaseExtractionException {
        DatabaseAccess postgresDatabaseAccess = new DatabaseAccess(
                postgresServer,
                Arrays.asList(dbDumpersFactory.getPostgresBinaryDump(), dbDumpersFactory.getPostgresBinaryRestore()),
                postgresFakeData,
                this.getDbSourceFromUri(postgresServer),
                this.getDbTargetFromUri(postgresServer)
        );
        DatabaseRef databaseServerPostgres = postgresDatabaseAccess.generateDatabaseRef();
        postgresDatabaseAccess.addCreateDatabaseCommand(new String[]{
                this.psqlBinary.getAbsolutePath(),
                String.format("--dbname=postgresql://%s:%s@%s:%s/%s",
                        databaseServerPostgres.getUser(),
                        databaseServerPostgres.getPassword(),
                        databaseServerPostgres.getHost(),
                        databaseServerPostgres.getPort(),
                        databaseServerPostgres.getDatabaseName()),
                "-c",
                "CREATE DATABASE " + DATABASE_SOURCE_NAME
        });
        postgresDatabaseAccess.addCreateDatabaseCommand(new String[]{
                this.psqlBinary.getAbsolutePath(),
                String.format("--dbname=postgresql://%s:%s@%s:%s/%s",
                        databaseServerPostgres.getUser(),
                        databaseServerPostgres.getPassword(),
                        databaseServerPostgres.getHost(),
                        databaseServerPostgres.getPort(),
                        databaseServerPostgres.getDatabaseName()),
                "-c",
                "CREATE DATABASE " + DATABASE_TARGET_NAME
        });
        postgresDatabaseAccess.addDropDatabaseCommands(new String[]{
                this.psqlBinary.getAbsolutePath(),
                String.format("--dbname=postgresql://%s:%s@%s:%s/%s",
                        databaseServerPostgres.getUser(),
                        databaseServerPostgres.getPassword(),
                        databaseServerPostgres.getHost(),
                        databaseServerPostgres.getPort(),
                        databaseServerPostgres.getDatabaseName()),
                "-c",
                "DROP DATABASE IF EXISTS " + DATABASE_SOURCE_NAME
        });
        postgresDatabaseAccess.addDropDatabaseCommands(new String[]{
                this.psqlBinary.getAbsolutePath(),
                String.format("--dbname=postgresql://%s:%s@%s:%s/%s",
                        databaseServerPostgres.getUser(),
                        databaseServerPostgres.getPassword(),
                        databaseServerPostgres.getHost(),
                        databaseServerPostgres.getPort(),
                        databaseServerPostgres.getDatabaseName()),
                "-c",
                "DROP DATABASE IF EXISTS " + DATABASE_TARGET_NAME
        });
        DatabaseAccess mysqlDatabaseAccess = new DatabaseAccess(
                mysqlServer,
                Arrays.asList(dbDumpersFactory.getMysqlBinaryDump(), dbDumpersFactory.getMysqlBinaryRestore()),
                mysqlFakeData,
                this.getDbSourceFromUri(mysqlServer),
                this.getDbTargetFromUri(mysqlServer)
        );

        DatabaseRef databaseServerMysql = mysqlDatabaseAccess.generateDatabaseRef();
        mysqlDatabaseAccess.addCreateDatabaseCommand(new String[]{
                this.mysqlBinary.getAbsolutePath(),
                "--host=" + databaseServerMysql.getHost(),
                "--port=" + databaseServerMysql.getPort(),
                "--user=" + databaseServerMysql.getUser(),
                "--password=" + databaseServerMysql.getPassword(),
                "-e",
                "CREATE DATABASE IF NOT EXISTS " + DATABASE_SOURCE_NAME
        });
        mysqlDatabaseAccess.addCreateDatabaseCommand(new String[]{
                this.mysqlBinary.getAbsolutePath(),
                "--host=" + databaseServerMysql.getHost(),
                "--port=" + databaseServerMysql.getPort(),
                "--user=" + databaseServerMysql.getUser(),
                "--password=" + databaseServerMysql.getPassword(),
                "-e",
                "CREATE DATABASE IF NOT EXISTS " + DATABASE_TARGET_NAME
        });
        mysqlDatabaseAccess.addDropDatabaseCommands(new String[]{
                this.mysqlBinary.getAbsolutePath(),
                "--host=" + databaseServerMysql.getHost(),
                "--port=" + databaseServerMysql.getPort(),
                "--user=" + databaseServerMysql.getUser(),
                "--password=" + databaseServerMysql.getPassword(),
                "-e",
                "DROP DATABASE IF EXISTS " + DATABASE_SOURCE_NAME
        });
        mysqlDatabaseAccess.addDropDatabaseCommands(new String[]{
                this.mysqlBinary.getAbsolutePath(),
                "--host=" + databaseServerMysql.getHost(),
                "--port=" + databaseServerMysql.getPort(),
                "--user=" + databaseServerMysql.getUser(),
                "--password=" + databaseServerMysql.getPassword(),
                "-e",
                "DROP DATABASE IF EXISTS " + DATABASE_TARGET_NAME
        });

        this.databaseAccessMap.put(
                DatabaseType.MONGODB,
                new DatabaseAccess(
                        mongoServer,
                        Arrays.asList(dbDumpersFactory.getMongodbBinaryDump(), dbDumpersFactory.getMongodbBinaryRestore()),
                        mongodbFakeData,
                        this.getDbSourceFromUri(mongoServer),
                        this.getDbTargetFromUri(mongoServer)
                )
        );

        this.databaseAccessMap.put(DatabaseType.POSTGRESQL, postgresDatabaseAccess);

        this.databaseAccessMap.put(
                DatabaseType.REDIS,
                new DatabaseAccess(
                        redisServer,
                        Arrays.asList(dbDumpersFactory.getRedisRutilBinary()),
                        redisFakeData,
                        redisServer,
                        redisServer
                )
        );

        this.databaseAccessMap.put(DatabaseType.MYSQL, mysqlDatabaseAccess);
    }
}
