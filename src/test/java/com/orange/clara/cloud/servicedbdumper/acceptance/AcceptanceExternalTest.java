package com.orange.clara.cloud.servicedbdumper.acceptance;

import com.orange.clara.cloud.servicedbdumper.Application;
import com.orange.clara.cloud.servicedbdumper.exception.DatabaseExtractionException;
import com.orange.clara.cloud.servicedbdumper.exception.ServiceKeyException;
import com.orange.clara.cloud.servicedbdumper.model.DatabaseType;
import org.cloudfoundry.community.servicebroker.exception.*;
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
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.regex.Pattern;
import java.util.stream.Stream;

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
 * Date: 25/05/2016
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(Application.class)
@WebIntegrationTest(randomPort = true)
@ActiveProfiles({"local", "integrationrealcf", "s3"})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@IfProfileValue(name = "test.groups", values = {"external-acceptance-tests"})
public class AcceptanceExternalTest extends AcceptanceLocalTest {

    @Value("${accept.cf.service.name.db.dumper:db-dumper-service-dev}")
    protected String serviceNameAcceptDbDumper;

    @Value("${accept.cf.service.plan.db.dumper:experimental}")
    protected String servicePlanAcceptDbDumper;

    @Override
    @Before
    public void init() throws DatabaseExtractionException {
        if (!this.isCfCliAvailableInPath()) {
            String skipMessage = "cf cli cannot be found in path please install it.";
            this.reportIntegration.setSkipped(true);
            this.reportIntegration.setSkippedReason(skipMessage);
            assumeTrue(skipMessage,
                    false);
        }
        if (!this.isCfCliPluginAvailable()) {
            String skipMessage = "db-dumper cli plugin cannot be found please install from https://github.com/Orange-OpenSource/db-dumper-cli-plugin/releases.";
            this.reportIntegration.setSkipped(true);
            this.reportIntegration.setSkippedReason(skipMessage);
            assumeTrue(skipMessage,
                    false);
        }
        super.init();
    }

    @Override
    protected void loadBeforeAction() {
        String loginCfCommand = String.format("cf login -a %s -u %s -p %s -o %s -s %s --skip-ssl-validation",
                cloudControllerUrl,
                cfAdminUser,
                cfAdminPassword,
                org,
                space
        );
        try {
            this.runCommand(loginCfCommand.split(" "));
        } catch (IOException | InterruptedException e) {
            fail(e.getMessage());
        }
        super.loadBeforeAction();
    }

    protected boolean isCfCliAvailableInPath() {
        String exec = "cf";
        return Stream.of(System.getenv("PATH").split(Pattern.quote(File.pathSeparator)))
                .map(Paths::get)
                .anyMatch(path -> Files.exists(path.resolve(exec)));
    }

    protected boolean isCfCliPluginAvailable() {
        Runtime rt = Runtime.getRuntime();
        Process proc = null;
        try {
            proc = rt.exec(this.dbDumperCli());
            proc.waitFor();
        } catch (IOException | InterruptedException e) {
            return false;
        }
        int exitVal = proc.exitValue();
        return exitVal == 0;
    }

    protected String dbDumperCli() {
        return "cf db-dumper -n " + this.serviceNameAcceptDbDumper;
    }

    @Override
    protected void deleteServiceInstance(String instanceId) throws ServiceBrokerAsyncRequiredException, ServiceBrokerException {
        String command = String.format("%s delete %s -f", this.dbDumperCli(), instanceId);
        try {
            this.runCommand(command.split(" "));
        } catch (IOException | InterruptedException e) {
            throw new ServiceBrokerException(e.getMessage(), e);
        }
    }

    @Override
    protected void createSourceDatabaseDump(DatabaseType databaseType) throws ServiceBrokerException, ServiceInstanceExistsException, ServiceBrokerAsyncRequiredException {
        String command = String.format("%s create %s --plan %s", this.dbDumperCli(), this.getDbParamsForDump(databaseType), this.servicePlanAcceptDbDumper);
        try {
            this.runCommand(command.split(" "));
        } catch (IOException | InterruptedException e) {
            throw new ServiceBrokerException(e.getMessage(), e);
        }
    }

    @Override
    protected void createTargetDatabaseDump(DatabaseType databaseType) throws ServiceBrokerException, ServiceInstanceExistsException, ServiceBrokerAsyncRequiredException {
        String command = String.format("%s create %s --plan %s", this.dbDumperCli(), this.getDbParamsForRestore(databaseType), this.servicePlanAcceptDbDumper);
        try {
            this.runCommand(command.split(" "));
        } catch (IOException | InterruptedException e) {
            throw new ServiceBrokerException(e.getMessage(), e);
        }
    }

    @Override
    protected void restoreSourceDatabaseDump(DatabaseType databaseType) throws ServiceBrokerException, ServiceInstanceExistsException, ServiceBrokerAsyncRequiredException, ServiceInstanceUpdateNotSupportedException, ServiceInstanceDoesNotExistException {
        String command = String.format("%s restore %s --source-instance %s --recent", this.dbDumperCli(), this.getDbParamsForRestore(databaseType), this.getDbParamsForDump(databaseType));
        try {
            this.runCommand(command.split(" "), true);
        } catch (IOException | InterruptedException e) {
            throw new ServiceBrokerException(e.getMessage(), e);
        }
    }

    @Override
    protected void loadServiceIds(DatabaseType databaseType) {
        this.serviceIdSource = this.getDbParamsForDump(databaseType);
        this.serviceIdTarget = this.getDbParamsForRestore(databaseType);
    }

    @Override
    protected InputStream getSourceStream(DatabaseType databaseType) throws DatabaseExtractionException, ServiceKeyException, IOException {
        String command = String.format("%s download %s --recent --original --stdout -k", this.dbDumperCli(), this.getDbParamsForDump(databaseType));
        try {
            Process process = this.runCommandLine(command.split(" "));
            return process.getInputStream();
        } catch (InterruptedException e) {
            throw new IOException(e.getMessage(), e);
        }
    }

    @Override
    protected InputStream getTargetStream(DatabaseType databaseType) throws DatabaseExtractionException, ServiceKeyException, IOException {
        String command = String.format("%s download %s --recent --original --stdout -k", this.dbDumperCli(), this.getDbParamsForRestore(databaseType));
        try {
            Process process = this.runCommandLine(command.split(" "));
            return process.getInputStream();
        } catch (InterruptedException e) {
            throw new IOException(e.getMessage(), e);
        }
    }
}
