package com.orange.clara.cloud.servicedbdumper.controllers;

import com.orange.clara.cloud.servicedbdumper.model.DatabaseDumpFile;
import com.orange.clara.cloud.servicedbdumper.model.DatabaseRef;
import com.orange.clara.cloud.servicedbdumper.repo.DatabaseDumpFileRepo;
import com.orange.clara.cloud.servicedbdumper.repo.DatabaseRefRepo;
import org.jclouds.blobstore.BlobStore;
import org.jclouds.blobstore.BlobStoreContext;
import org.jclouds.blobstore.domain.Blob;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.List;

/**
 * Copyright (C) 2015 Orange
 * <p/>
 * This software is distributed under the terms and conditions of the 'MIT'
 * license which can be found in the file 'LICENSE' in this package distribution
 * or at 'http://opensource.org/licenses/MIT'.
 * <p/>
 * Author: Arthur Halet
 * Date: 14/10/2015
 */
@RestController
@RequestMapping(value = "/manage")
public class ManagerController {

    @Autowired
    @Qualifier(value = "blobStoreContext")
    private BlobStoreContext blobStoreContext;

    @Autowired
    @Qualifier(value = "bucketName")
    private String bucketName;

    @Autowired
    private DatabaseDumpFileRepo databaseDumpFileRepo;

    @Autowired
    private DatabaseRefRepo databaseRefRepo;

    @RequestMapping("/raw/{databaseName}/{fileName:.*}")
    public String show(@PathVariable String databaseName, @PathVariable String fileName) throws IOException {
        fileName = databaseName + "/" + fileName;
        BlobStore blobStore = blobStoreContext.getBlobStore();
        Blob blob = blobStore.getBlob(this.bucketName, fileName);
        InputStream inputStream = blob.getPayload().openStream();

        String content = "";
        BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
        for (String line = br.readLine(); line != null; line = br.readLine()) {
            content += line;
            content += "<br/>";
        }
        br.close();

        return content;
    }

    @RequestMapping(value = "/download/{databaseName}/{fileName:.*}", method = RequestMethod.GET)
    public ResponseEntity<InputStreamResource> download(@PathVariable String databaseName, @PathVariable String fileName)
            throws IOException {
        fileName = databaseName + "/" + fileName;
        BlobStore blobStore = blobStoreContext.getBlobStore();
        Blob blob = blobStore.getBlob(this.bucketName, fileName);

        HttpHeaders respHeaders = new HttpHeaders();

        respHeaders.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        respHeaders.setContentLength(blob.getPayload().getContentMetadata().getContentLength());
        respHeaders.setContentDispositionFormData("attachment", fileName);

        InputStream inputStream = blob.getPayload().openStream();

        InputStreamResource isr = new InputStreamResource(inputStream);
        return new ResponseEntity<>(isr, respHeaders, HttpStatus.OK);
    }

    @RequestMapping("/list/{databaseName}")
    public String list(@PathVariable String databaseName) throws IOException {
        String content = "";
        DatabaseRef databaseRef = null;
        if (!this.databaseRefRepo.exists(databaseName)) {
            return "Database " + databaseName + " doesn't exist.";
        }
        databaseRef = this.databaseRefRepo.findOne(databaseName);
        List<DatabaseDumpFile> databaseDumpFiles = this.databaseDumpFileRepo.findByDatabaseRefOrderByCreatedAtDesc(databaseRef);
        if (databaseDumpFiles == null) {
            return "No dump files for database " + databaseName + ".";
        }
        SimpleDateFormat format = new SimpleDateFormat("dd-MM-yyyy");
        String filename;
        for (DatabaseDumpFile databaseDumpFile : databaseDumpFiles) {
            filename = databaseRef.getName() + "/" + databaseDumpFile.getFileName();
            content += "file from date: " + format.format(databaseDumpFile.getCreatedAt());
            content += " <a href=\"/manage/raw/" + filename + "\">show</a> <a href=\"/manage/download/" + filename + "\">download</a>";
        }
        return content;
    }
}
