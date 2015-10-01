package com.orange.clara.cloud.controllers;

import com.orange.clara.cloud.model.DatabaseRef;
import com.orange.clara.cloud.repo.DatabaseRefRepo;
import org.jclouds.blobstore.BlobStoreContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import java.net.URI;

/**
 * Copyright (C) 2015 Orange
 * <p/>
 * This software is distributed under the terms and conditions of the 'MIT'
 * license which can be found in the file 'LICENSE' in this package distribution
 * or at 'http://opensource.org/licenses/MIT'.
 * <p/>
 * Author: Arthur Halet
 * Date: 01/10/2015
 */
abstract public class AbstractDbController {
    @Autowired
    @Qualifier(value = "blobStoreContext")
    private BlobStoreContext blobStoreContext;

    @Autowired
    private DatabaseRefRepo databaseRefRepo;

    protected DatabaseRef getDatabaseRefFromUrl(String dbUrl, String serviceName) {
        DatabaseRef databaseRef = new DatabaseRef(serviceName, URI.create(dbUrl));
        DatabaseRef databaseRefDao = null;
        if (!this.databaseRefRepo.exists(serviceName)) {
            this.databaseRefRepo.save(databaseRef);
            return databaseRef;
        }
        databaseRefDao = this.databaseRefRepo.findOne(serviceName);
        this.updateDatabaseRef(databaseRef, databaseRefDao);
        databaseRef = databaseRefDao;

        return databaseRef;
    }

    protected void updateDatabaseRef(DatabaseRef databaseRefTemp, DatabaseRef databaseRefDao) {
        if (databaseRefDao.equals(databaseRefTemp)) {
            return;
        }
        databaseRefDao.setDatabaseName(databaseRefTemp.getDatabaseName());
        databaseRefDao.setHost(databaseRefTemp.getHost());
        databaseRefDao.setPassword(databaseRefTemp.getPassword());
        databaseRefDao.setPort(databaseRefTemp.getPort());
        databaseRefDao.setType(databaseRefTemp.getType());
        databaseRefDao.setUser(databaseRefTemp.getUser());
        this.databaseRefRepo.save(databaseRefDao);
    }

    public void setBlobStoreContext(BlobStoreContext blobStoreContext) {
        this.blobStoreContext = blobStoreContext;
    }
}
