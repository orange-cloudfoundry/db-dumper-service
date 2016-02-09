package com.orange.clara.cloud.servicedbdumper.controllers;

import com.orange.clara.cloud.servicedbdumper.dbdumper.running.Deleter;
import com.orange.clara.cloud.servicedbdumper.filer.Filer;
import com.orange.clara.cloud.servicedbdumper.model.DatabaseDumpFile;
import com.orange.clara.cloud.servicedbdumper.model.DatabaseRef;
import com.orange.clara.cloud.servicedbdumper.repo.DatabaseDumpFileRepo;
import com.orange.clara.cloud.servicedbdumper.repo.DatabaseRefRepo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

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
@Controller
@RequestMapping(value = "/manage")
public class ManagerController {
    private Logger logger = LoggerFactory.getLogger(ManagerController.class);
    @Autowired
    private DatabaseRefRepo databaseRefRepo;

    @Autowired
    @Qualifier(value = "filer")
    private Filer filer;


    @Autowired
    private DatabaseDumpFileRepo databaseDumpFileRepo;

    @Autowired
    @Qualifier(value = "deleter")
    private Deleter deleter;

    @RequestMapping("/raw/{databaseName}/{fileName:.*}")
    @ResponseBody
    public String raw(@PathVariable String databaseName, @PathVariable String fileName) throws IOException {
        this.checkDatabase(databaseName);
        fileName = databaseName + "/" + fileName;
        InputStream inputStream = this.filer.retrieveWithStream(fileName);

        String content = "";
        BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
        for (String line = br.readLine(); line != null; line = br.readLine()) {
            content += line;
            content += "\n";
        }
        br.close();

        return content;
    }

    private void checkDatabase(String databaseName) {
        DatabaseRef databaseRef = this.databaseRefRepo.findOne(databaseName);
        if (databaseRef == null) {
            throw new IllegalArgumentException(String.format("Cannot find database with name '%s'", databaseName));
        }
        if (databaseRef.isDeleted()) {
            throw new IllegalArgumentException(String.format("Database with name '%s' has been deleted", databaseName));
        }
    }

    @RequestMapping("/show/{databaseName}/{fileName:.*}")
    public String show(@PathVariable String databaseName, @PathVariable String fileName, Model model) throws IOException {
        this.checkDatabase(databaseName);
        String finalFileName = databaseName + "/" + fileName;
        InputStream inputStream = this.filer.retrieveWithStream(finalFileName);

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

    @RequestMapping("/delete/{dumpFileId:[0-9]+}")
    public String delete(@PathVariable Integer dumpFileId, Model model) throws IOException {
        DatabaseDumpFile databaseDumpFile = this.databaseDumpFileRepo.findOne(dumpFileId);
        if (databaseDumpFile == null) {
            throw new IllegalArgumentException(String.format("Cannot find dump file with id '%s'", dumpFileId));
        }
        this.checkDatabase(databaseDumpFile.getDatabaseRef().getName());
        DatabaseRef databaseRef = databaseDumpFile.getDatabaseRef();
        this.deleter.delete(databaseDumpFile);
        return String.format("redirect:/manage/list/database/%s", databaseRef.getName());
    }


    @RequestMapping(value = "/download/{databaseName}/{fileName:.*}", method = RequestMethod.GET)
    public ResponseEntity<InputStreamResource> download(@PathVariable String databaseName, @PathVariable String fileName)
            throws IOException {
        this.checkDatabase(databaseName);
        fileName = databaseName + "/" + fileName;

        HttpHeaders respHeaders = new HttpHeaders();

        respHeaders.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        respHeaders.setContentLength(this.filer.getContentLength(fileName));
        respHeaders.setContentDispositionFormData("attachment", fileName);

        InputStream inputStream = this.filer.retrieveWithOriginalStream(fileName);

        InputStreamResource isr = new InputStreamResource(inputStream);
        return new ResponseEntity<>(isr, respHeaders, HttpStatus.OK);
    }
}
