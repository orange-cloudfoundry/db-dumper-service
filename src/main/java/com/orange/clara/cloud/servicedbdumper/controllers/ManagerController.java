package com.orange.clara.cloud.servicedbdumper.controllers;

import com.orange.clara.cloud.servicedbdumper.filer.Filer;
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
    @Qualifier(value = "filer")
    private Filer filer;

    @RequestMapping("/raw/{databaseName}/{fileName:.*}")
    public String show(@PathVariable String databaseName, @PathVariable String fileName) throws IOException {
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

    @RequestMapping(value = "/download/{databaseName}/{fileName:.*}", method = RequestMethod.GET)
    public ResponseEntity<InputStreamResource> download(@PathVariable String databaseName, @PathVariable String fileName)
            throws IOException {
        fileName = databaseName + "/" + fileName;

        HttpHeaders respHeaders = new HttpHeaders();

        respHeaders.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        respHeaders.setContentLength(this.filer.getContentLength(fileName));
        respHeaders.setContentDispositionFormData("attachment", fileName);

        InputStream inputStream = this.filer.retrieveWithStream(fileName);

        InputStreamResource isr = new InputStreamResource(inputStream);
        return new ResponseEntity<>(isr, respHeaders, HttpStatus.OK);
    }
}
