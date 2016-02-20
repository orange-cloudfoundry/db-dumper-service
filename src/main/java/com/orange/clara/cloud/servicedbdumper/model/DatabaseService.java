package com.orange.clara.cloud.servicedbdumper.model;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;

/**
 * Copyright (C) 2016 Orange
 * <p>
 * This software is distributed under the terms and conditions of the 'Apache-2.0'
 * license which can be found in the file 'LICENSE' in this package distribution
 * or at 'https://opensource.org/licenses/Apache-2.0'.
 * <p>
 * Author: Arthur Halet
 * Date: 18/02/2016
 */
@Entity
public class DatabaseService {
    @Id
    protected String uuid;

    protected String name;

    protected String space;

    protected String org;

    protected String serviceKeyGuid;

    @OneToOne
    @JoinColumn(name = "database_ref_id")
    private DatabaseRef databaseRef;

    public DatabaseService() {

    }

    public DatabaseService(String uuid, String name, String org, String space) {
        this.uuid = uuid;
        this.name = name;
        this.space = space;
        this.org = org;
    }

    public DatabaseService(String uuid, String name, String org, String space, String serviceKeyGuid) {
        this(uuid, name, org, space);
        this.serviceKeyGuid = serviceKeyGuid;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSpace() {
        return space;
    }

    public void setSpace(String space) {
        this.space = space;
    }

    public String getOrg() {
        return org;
    }

    public void setOrg(String org) {
        this.org = org;
    }

    public DatabaseRef getDatabaseRef() {
        return databaseRef;
    }

    public void setDatabaseRef(DatabaseRef databaseRef) {
        this.databaseRef = databaseRef;
        this.databaseRef.setDatabaseService(this);
    }

    public String getServiceKeyGuid() {
        return serviceKeyGuid;
    }

    public void setServiceKeyGuid(String serviceKeyGuid) {
        this.serviceKeyGuid = serviceKeyGuid;
    }

    @Override
    public int hashCode() {
        return uuid.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DatabaseService that = (DatabaseService) o;

        return uuid.equals(that.uuid);

    }
}
