package com.orange.clara.cloud.servicedbdumper.model;

import javax.persistence.*;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

/**
 * Copyright (C) 2015 Orange
 * <p/>
 * This software is distributed under the terms and conditions of the 'MIT'
 * license which can be found in the file 'LICENSE' in this package distribution
 * or at 'http://opensource.org/licenses/MIT'.
 * <p/>
 * Author: Arthur Halet
 * Date: 03/06/2015
 */
@Entity
public class DatabaseRef {
    @Id
    protected String name;
    protected String user;
    protected String password;
    protected String host;
    protected String databaseName;

    @Enumerated(EnumType.STRING)
    protected DatabaseType type;

    @OneToMany(mappedBy = "databaseRef")
    protected List<DatabaseDumpFile> databaseDumpFiles;

    protected Integer port;

    @OneToMany(mappedBy = "databaseRef")
    private List<DbDumperServiceInstance> dbDumperServiceInstances;

    public DatabaseRef() {
        this.databaseDumpFiles = new ArrayList<>();
        this.dbDumperServiceInstances = new ArrayList<>();
    }

    public DatabaseRef(String serviceName, URI databaseUri) {
        this();
        this.extractDatabaseType(databaseUri.getScheme());
        this.name = serviceName;
        this.host = databaseUri.getHost();
        String[] userInfo = databaseUri.getUserInfo().split(":");
        this.user = userInfo[0];
        this.password = userInfo[1];
        this.port = databaseUri.getPort();
        this.databaseName = databaseUri.getPath().substring(1);
    }

    public DatabaseRef(String name) {
        this();
        this.name = name;
    }

    private void extractDatabaseType(String databaseTypeName) {
        for (DatabaseType databaseType : DatabaseType.values()) {
            if (databaseTypeName.matches(databaseType.getMatcher())) {
                this.type = databaseType;
                break;
            }
        }
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getDatabaseName() {
        return databaseName;
    }

    public void setDatabaseName(String database) {
        this.databaseName = database;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public DatabaseType getType() {
        return type;
    }

    public void setType(DatabaseType type) {
        this.type = type;
    }

    public List<DatabaseDumpFile> getDatabaseDumpFiles() {
        return databaseDumpFiles;
    }

    public void setDatabaseDumpFiles(List<DatabaseDumpFile> databaseDumpFiles) {
        this.databaseDumpFiles = databaseDumpFiles;
    }

    public void addDbDumperServiceInstance(DbDumperServiceInstance dbDumperServiceInstance) {
        if (this.dbDumperServiceInstances.contains(dbDumperServiceInstance)) {
            return;
        }
        this.dbDumperServiceInstances.add(dbDumperServiceInstance);
    }

    public void removeDbDumperServiceInstance(DbDumperServiceInstance dbDumperServiceInstance) {
        if (!this.dbDumperServiceInstances.contains(dbDumperServiceInstance)) {
            return;
        }
        this.dbDumperServiceInstances.remove(dbDumperServiceInstance);
    }

    public void addDatabaseDumpFile(DatabaseDumpFile databaseDumpFile) {
        if (this.databaseDumpFiles.contains(databaseDumpFile)) {
            return;
        }
        this.databaseDumpFiles.add(databaseDumpFile);
    }

    public void removeDatabaseDumpFile(DatabaseDumpFile databaseDumpFile) {
        if (!this.databaseDumpFiles.contains(databaseDumpFile)) {
            return;
        }
        this.databaseDumpFiles.remove(databaseDumpFile);
    }

    public List<DbDumperServiceInstance> getDbDumperServiceInstance() {
        return dbDumperServiceInstances;
    }

    public void setDbDumperServiceInstance(List<DbDumperServiceInstance> dbDumperServiceInstanceBindings) {
        this.dbDumperServiceInstances = dbDumperServiceInstanceBindings;
    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (user != null ? user.hashCode() : 0);
        result = 31 * result + (password != null ? password.hashCode() : 0);
        result = 31 * result + (host != null ? host.hashCode() : 0);
        result = 31 * result + (databaseName != null ? databaseName.hashCode() : 0);
        result = 31 * result + (type != null ? type.hashCode() : 0);
        result = 31 * result + (port != null ? port.hashCode() : 0);
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DatabaseRef that = (DatabaseRef) o;

        if (name != null ? !name.equals(that.name) : that.name != null) return false;
        if (user != null ? !user.equals(that.user) : that.user != null) return false;
        if (password != null ? !password.equals(that.password) : that.password != null) return false;
        if (host != null ? !host.equals(that.host) : that.host != null) return false;
        if (databaseName != null ? !databaseName.equals(that.databaseName) : that.databaseName != null) return false;
        if (type != that.type) return false;
        return !(port != null ? !port.equals(that.port) : that.port != null);

    }

}