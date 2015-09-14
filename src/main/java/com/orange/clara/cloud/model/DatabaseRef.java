package com.orange.clara.cloud.model;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import java.net.URI;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

    protected Integer port;

    public DatabaseRef() {
    }

    public DatabaseRef(String serviceName, URI databaseUri) {
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
        this.name = name;
    }

    private void extractDatabaseType(String databaseTypeName) {
        for (DatabaseType databaseType : DatabaseType.values()) {
            Pattern pattern = Pattern.compile(databaseType.getMatcher());
            Matcher matcher = pattern.matcher(databaseTypeName);
            if (matcher.find()) {
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
}
