package com.orange.clara.cloud.servicedbdumper.controllers;

import com.google.common.collect.Lists;
import com.orange.clara.cloud.servicedbdumper.model.DatabaseRef;
import com.orange.clara.cloud.servicedbdumper.repo.DatabaseRefRepo;
import org.jclouds.blobstore.BlobStore;
import org.jclouds.blobstore.BlobStoreContext;
import org.jclouds.blobstore.domain.Blob;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

/**
 * Copyright (C) 2015 Orange
 * <p/>
 * This software is distributed under the terms and conditions of the 'MIT'
 * license which can be found in the file 'LICENSE' in this package distribution
 * or at 'http://opensource.org/licenses/MIT'.
 * <p/>
 * Author: Arthur Halet
 * Date: 15/10/2015
 */
@Controller
@RequestMapping(value = "/manage")
public class InterfaceController {
    @Autowired
    @Qualifier(value = "blobStoreContext")
    private BlobStoreContext blobStoreContext;

    @Autowired
    @Qualifier(value = "bucketName")
    private String bucketName;

    @Autowired
    private DatabaseRefRepo databaseRefRepo;

    @RequestMapping("/show/{databaseName}/{fileName:.*}")
    public String show(@PathVariable String databaseName, @PathVariable String fileName, Model model) throws IOException {
        fileName = databaseName + "/" + fileName;
        BlobStore blobStore = blobStoreContext.getBlobStore();
        Blob blob = blobStore.getBlob(this.bucketName, fileName);
        InputStream inputStream = blob.getPayload().openStream();

        String content = "";
        BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
        for (String line = br.readLine(); line != null; line = br.readLine()) {
            content += line;
            content += "\n";
        }
        br.close();
        model.addAttribute("databaseName", databaseName);
        model.addAttribute("fileName", fileName);
        model.addAttribute("sql", content);
        return "show";
    }

    @RequestMapping("/list")
    public String list(Model model) throws IOException {
        List<DatabaseRef> databaseRefs = Lists.newArrayList(this.databaseRefRepo.findAll());
        model.addAttribute("databaseRefs", databaseRefs);
        return "listfiles";
    }

    @RequestMapping("")
    public String welcome(Model model) {
        return "welcome";
    }
}
