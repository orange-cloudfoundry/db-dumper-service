package com.orange.clara.cloud.servicedbdumper.model;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

/**
 * Copyright (C) 2015 Orange
 * <p/>
 * This software is distributed under the terms and conditions of the 'Apache-2.0'
 * license which can be found in the file 'LICENSE' in this package distribution
 * or at 'https://opensource.org/licenses/Apache-2.0'.
 * <p/>
 * Author: Arthur Halet
 * Date: 12/10/2015
 */
@Entity
public class DbDumperServiceInstanceBinding {

    @Id
    private String id;

    @ManyToOne
    @JoinColumn(name = "db_dumper_service_instance_id")
    private DbDumperServiceInstance dbDumperServiceInstance;

    private String appGuid;

    public DbDumperServiceInstanceBinding() {
    }

    public DbDumperServiceInstanceBinding(String id, DbDumperServiceInstance dbDumperServiceInstance, String appGuid) {
        this.id = id;
        this.dbDumperServiceInstance = dbDumperServiceInstance;
        this.appGuid = appGuid;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }


    public String getAppGuid() {
        return appGuid;
    }

    public void setAppGuid(String appGuid) {
        this.appGuid = appGuid;
    }

    public DbDumperServiceInstance getDbDumperServiceInstance() {
        return dbDumperServiceInstance;
    }

    public void setDbDumperServiceInstance(DbDumperServiceInstance dbDumperServiceInstance) {
        this.dbDumperServiceInstance = dbDumperServiceInstance;
        dbDumperServiceInstance.addDbDumperServiceInstanceBinding(this);
    }
}
