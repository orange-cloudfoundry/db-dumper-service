package com.orange.clara.cloud.servicedbdumper.model;

import com.orange.clara.cloud.servicedbdumper.exception.DatabaseExtractionException;
import com.orange.clara.cloud.servicedbdumper.security.CryptoConverter;

import javax.persistence.*;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

/**
 * Copyright (C) 2015 Orange
 * <p>
 * This software is distributed under the terms and conditions of the 'Apache-2.0'
 * license which can be found in the file 'LICENSE' in this package distribution
 * or at 'https://opensource.org/licenses/Apache-2.0'.
 * <p>
 * Author: Arthur Halet
 * Date: 03/06/2015
 */
@Entity
public class DatabaseRef {
    @Id
    protected String name;
    protected String user;
    @Convert(converter = CryptoConverter.class)
    protected String password;
    protected String host;
    protected String databaseName;
    @Enumerated(EnumType.STRING)
    protected DatabaseType type;
    protected Integer port;

    @OneToOne
    @JoinColumn(name = "database_service_id")
    private DatabaseService databaseService;

    @OneToMany(mappedBy = "databaseRef", fetch = FetchType.EAGER)
    private List<DbDumperServiceInstance> dbDumperServiceInstances;

    public DatabaseRef() {
        this.dbDumperServiceInstances = new ArrayList<>();
    }

    public DatabaseRef(String serviceName, URI databaseUri) throws DatabaseExtractionException {
        this();
        this.extractDatabaseType(databaseUri.getScheme());
        this.name = serviceName;
        this.host = databaseUri.getHost();
        this.user = "";
        this.password = "";
        this.databaseName = "";
        if (databaseUri.getPort() <= 0) {
            this.port = this.type.getDefaultPort();
        } else {
            this.port = databaseUri.getPort();
        }
        if (databaseUri.getPath() != null && !databaseUri.getPath().isEmpty()) {
            this.databaseName = databaseUri.getPath().substring(1);
        }
        if (databaseUri.getUserInfo() == null || databaseUri.getUserInfo().isEmpty()) {
            return;
        }
        String[] userInfo = databaseUri.getUserInfo().split(":");
        this.user = userInfo[0];
        if (userInfo.length > 1) {
            this.password = userInfo[1];
        }
    }

    public DatabaseRef(String name) {
        this();
        this.name = name;
    }

    private void extractDatabaseType(String databaseTypeName) throws DatabaseExtractionException {
        if (databaseTypeName == null) {
            throw new DatabaseExtractionException("Uri is malformed.");
        }
        for (DatabaseType databaseType : DatabaseType.values()) {
            if (databaseTypeName.matches(databaseType.getMatcher())) {
                this.type = databaseType;
                break;
            }
        }
        if (this.type == null) {
            throw new DatabaseExtractionException("The database driver '" + databaseTypeName + "' is not supported.");
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

    public List<DbDumperServiceInstance> getDbDumperServiceInstances() {
        return dbDumperServiceInstances;
    }

    public void setDbDumperServiceInstances(List<DbDumperServiceInstance> dbDumperServiceInstanceBindings) {
        this.dbDumperServiceInstances = dbDumperServiceInstanceBindings;
    }

    public String getInUrlFormat() {
        return this.type.toString().toLowerCase() + "://" + this.user + "@" + this.host + ":" + this.port;
    }

    public DatabaseService getDatabaseService() {
        return databaseService;
    }

    public void setDatabaseService(DatabaseService databaseService) {
        this.databaseService = databaseService;
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DatabaseRef)) return false;

        DatabaseRef that = (DatabaseRef) o;

        return name.equals(that.name);

    }

    @Override
    public String toString() {
        return "DatabaseRef{" +
                "name='" + name + '\'' +
                ", user='" + user + '\'' +
                ", password='" + password + '\'' +
                ", host='" + host + '\'' +
                ", databaseName='" + databaseName + '\'' +
                ", type=" + type +
                ", port=" + port +
                ", databaseService=" + databaseService +
                ", dbDumperServiceInstances=" + dbDumperServiceInstances +
                '}';
    }
}
