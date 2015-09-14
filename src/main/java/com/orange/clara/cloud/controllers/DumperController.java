package com.orange.clara.cloud.controllers;

import com.orange.clara.cloud.dbdump.action.Dumper;
import com.orange.clara.cloud.model.DatabaseRef;
import com.orange.clara.cloud.repo.DatabaseRefRepo;
import org.jclouds.blobstore.BlobStoreContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.net.URI;

@RestController
public class DumperController {

    @Autowired
    @Qualifier(value = "dumper")
    private Dumper dumper;

    @Autowired
    @Qualifier(value = "blobStoreContext")
    private BlobStoreContext blobStoreContext;

    @Autowired
    private DatabaseRefRepo databaseRefRepo;

    @RequestMapping("/")
    public String index() {
        return "Greetings from Spring Boot!";
    }

    @RequestMapping(value = "/dumpdb", method = RequestMethod.GET)
    public String dump(@RequestParam String dbUrl) throws IOException, InterruptedException {
        DatabaseRef databaseRef = this.getDatabaseRefFromUrl(dbUrl);
        return this.dumper.action(databaseRef);
    }

    private DatabaseRef getDatabaseRefFromUrl(String dbUrl) {
        String serviceName = "temp";
        DatabaseRef databaseRef = new DatabaseRef("temp", URI.create(dbUrl));
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

    private void updateDatabaseRef(DatabaseRef databaseRefTemp, DatabaseRef databaseRefDao) {
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
