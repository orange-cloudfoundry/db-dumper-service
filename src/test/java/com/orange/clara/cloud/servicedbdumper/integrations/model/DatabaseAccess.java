package com.orange.clara.cloud.servicedbdumper.integrations.model;

import com.google.common.collect.Lists;
import com.orange.clara.cloud.servicedbdumper.exception.DatabaseExtractionException;
import com.orange.clara.cloud.servicedbdumper.model.DatabaseRef;

import java.io.File;
import java.net.URI;
import java.util.List;

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
public class DatabaseAccess {
    private String server;
    private List<File> binaries;
    private File fakeDataFile;
    private String databaseSourceUri;
    private String databaseTargetUri;
    private String serviceName;
    private String serviceSourceInstanceName;
    private String serviceTargetInstanceName;
    private String servicePlan;
    private List<String[]> createDatabaseCommands;
    private List<String[]> dropDatabaseCommands;

    public DatabaseAccess(String server, List<File> binaries, File fakeDataFile, String databaseSourceUri, String databaseTargetUri) {
        this.server = server;
        this.binaries = binaries;
        this.fakeDataFile = fakeDataFile;
        this.databaseSourceUri = databaseSourceUri;
        this.databaseTargetUri = databaseTargetUri;
        this.createDatabaseCommands = Lists.newArrayList();
        this.dropDatabaseCommands = Lists.newArrayList();
    }

    public String getServer() {
        return server;
    }

    public void setServer(String server) {
        this.server = server;
    }

    public List<File> getBinaries() {
        return binaries;
    }

    public void setBinaries(List<File> binaries) {
        this.binaries = binaries;
    }


    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getServiceSourceInstanceName() {
        return serviceSourceInstanceName;
    }

    public void setServiceSourceInstanceName(String serviceSourceInstanceName) {
        this.serviceSourceInstanceName = serviceSourceInstanceName;
    }

    public String getServicePlan() {
        return servicePlan;
    }

    public void setServicePlan(String servicePlan) {
        this.servicePlan = servicePlan;
    }

    public File getFakeDataFile() {
        return fakeDataFile;
    }

    public void setFakeDataFile(File fakeDataFile) {
        this.fakeDataFile = fakeDataFile;
    }

    public List<String[]> getCreateDatabaseCommands() {
        return createDatabaseCommands;
    }

    public void setCreateDatabaseCommands(List<String[]> createDatabaseCommands) {
        this.createDatabaseCommands = createDatabaseCommands;
    }

    public void addCreateDatabaseCommand(String[] command) {
        this.createDatabaseCommands.add(command);
    }

    public List<String[]> getDropDatabaseCommands() {
        return dropDatabaseCommands;
    }

    public void setDropDatabaseCommands(List<String[]> dropDatabaseCommands) {
        this.dropDatabaseCommands = dropDatabaseCommands;
    }

    public void addDropDatabaseCommands(String[] command) {
        this.dropDatabaseCommands.add(command);
    }

    public DatabaseRef generateDatabaseRef() throws DatabaseExtractionException {
        return new DatabaseRef("server-localhost", URI.create(server));
    }

    public String getDatabaseSourceUri() {
        return databaseSourceUri;
    }

    public void setDatabaseSourceUri(String databaseSourceUri) {
        this.databaseSourceUri = databaseSourceUri;
    }

    public String getDatabaseTargetUri() {
        return databaseTargetUri;
    }

    public void setDatabaseTargetUri(String databaseTargetUri) {
        this.databaseTargetUri = databaseTargetUri;
    }

    public String getServiceTargetInstanceName() {
        return serviceTargetInstanceName;
    }

    public void setServiceTargetInstanceName(String serviceTargetInstanceName) {
        this.serviceTargetInstanceName = serviceTargetInstanceName;
    }

}
