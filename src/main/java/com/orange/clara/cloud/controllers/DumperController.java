package com.orange.clara.cloud.controllers;

import com.google.common.base.Joiner;
import com.google.common.io.Files;
import com.orange.clara.cloud.dbdump.DatabaseDumper;
import com.orange.clara.cloud.dbdump.DbDumpersFactory;
import com.orange.clara.cloud.dbdump.Dumper;
import com.orange.clara.cloud.model.DatabaseRef;
import com.orange.clara.cloud.repo.DatabaseRefRepo;
import org.jclouds.blobstore.BlobStoreContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

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
        String serviceName = "temp";
        DatabaseRef databaseRef = null;
        if (!this.databaseRefRepo.exists(serviceName)) {
            databaseRef = new DatabaseRef("temp", URI.create(dbUrl));
            this.databaseRefRepo.save(databaseRef);
        } else {
            databaseRef = this.databaseRefRepo.findOne(serviceName);
        }
        return this.dumper.dump(databaseRef);
    }

    public void setBlobStoreContext(BlobStoreContext blobStoreContext) {
        this.blobStoreContext = blobStoreContext;
    }
}
