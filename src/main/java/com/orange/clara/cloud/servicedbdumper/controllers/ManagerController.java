package com.orange.clara.cloud.servicedbdumper.controllers;

import com.orange.clara.cloud.servicedbdumper.dbdumper.Deleter;
import com.orange.clara.cloud.servicedbdumper.exception.DumpFileShowException;
import com.orange.clara.cloud.servicedbdumper.exception.UserAccessRightException;
import com.orange.clara.cloud.servicedbdumper.filer.Filer;
import com.orange.clara.cloud.servicedbdumper.model.DatabaseDumpFile;
import com.orange.clara.cloud.servicedbdumper.model.DatabaseRef;
import com.orange.clara.cloud.servicedbdumper.repo.DatabaseDumpFileRepo;
import com.orange.clara.cloud.servicedbdumper.repo.DatabaseRefRepo;
import com.orange.clara.cloud.servicedbdumper.security.UserAccessRight;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.io.*;
import java.nio.charset.Charset;
import java.util.Base64;

/**
 * Copyright (C) 2015 Orange
 * <p>
 * This software is distributed under the terms and conditions of the 'Apache-2.0'
 * license which can be found in the file 'LICENSE' in this package distribution
 * or at 'https://opensource.org/licenses/Apache-2.0'.
 * <p>
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
    private AuthenticationManagerBuilder authenticationManagerBuilder;

    @Autowired
    @Qualifier(value = "filer")
    private Filer filer;

    @Autowired
    @Qualifier("userAccessRight")
    private UserAccessRight userAccessRight;


    @Autowired
    private DatabaseDumpFileRepo databaseDumpFileRepo;

    @Autowired
    @Qualifier(value = "deleter")
    private Deleter deleter;

    @RequestMapping("/raw/{dumpFileId:[0-9]+}")
    @ResponseBody
    public String raw(@PathVariable Integer dumpFileId) throws IOException, UserAccessRightException, DumpFileShowException {
        DatabaseDumpFile databaseDumpFile = this.databaseDumpFileRepo.findOne(dumpFileId);
        if (databaseDumpFile == null) {
            throw new IllegalArgumentException(String.format("Cannot find dump file with id '%s'", dumpFileId));
        }
        this.checkDatabaseWithAccessRight(databaseDumpFile.getDatabaseRef());
        this.checkDumpShowable(databaseDumpFile);
        String databaseName = databaseDumpFile.getDatabaseRef().getName();
        String fileName = databaseDumpFile.getFileName();
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

    private void checkDatabaseWithAccessRight(DatabaseRef databaseRef) throws UserAccessRightException {
        this.checkDatabase(databaseRef);
        if (!this.userAccessRight.haveAccessToServiceInstance(databaseRef.getDbDumperServiceInstances())) {
            throw new UserAccessRightException("You don't have access to this instance");
        }
    }

    private void checkDatabase(DatabaseRef databaseRef) throws UserAccessRightException {
        if (databaseRef.isDeleted()) {
            throw new IllegalArgumentException(String.format("Database with name '%s' has been deleted", databaseRef.getName()));
        }
    }

    private void checkDumpShowable(DatabaseDumpFile databaseDumpFile) throws DumpFileShowException {
        if (!databaseDumpFile.isShowable()) {
            throw new DumpFileShowException(databaseDumpFile);
        }
    }

    @RequestMapping("/show/{dumpFileId:[0-9]+}")
    public String show(@PathVariable Integer dumpFileId, Model model) throws IOException, UserAccessRightException, DumpFileShowException {
        DatabaseDumpFile databaseDumpFile = this.databaseDumpFileRepo.findOne(dumpFileId);
        if (databaseDumpFile == null) {
            throw new IllegalArgumentException(String.format("Cannot find dump file with id '%s'", dumpFileId));
        }
        this.checkDatabaseWithAccessRight(databaseDumpFile.getDatabaseRef());
        this.checkDumpShowable(databaseDumpFile);
        String databaseName = databaseDumpFile.getDatabaseRef().getName();
        String fileName = databaseDumpFile.getFileName();
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
        model.addAttribute("id", dumpFileId);
        model.addAttribute("sql", content);
        return "show";
    }

    @RequestMapping("/delete/{dumpFileId:[0-9]+}")
    public String delete(@PathVariable Integer dumpFileId, Model model) throws IOException, UserAccessRightException {
        DatabaseDumpFile databaseDumpFile = this.databaseDumpFileRepo.findOne(dumpFileId);
        if (databaseDumpFile == null) {
            throw new IllegalArgumentException(String.format("Cannot find dump file with id '%s'", dumpFileId));
        }
        this.checkDatabaseWithAccessRight(databaseDumpFile.getDatabaseRef());
        DatabaseRef databaseRef = databaseDumpFile.getDatabaseRef();
        this.deleter.delete(databaseDumpFile);
        return String.format("redirect:/manage/list/database/%s", databaseRef.getName());
    }


    @RequestMapping(value = "/download/{dumpFileId:[0-9]+}", method = RequestMethod.GET)
    public ResponseEntity<InputStreamResource> download(@PathVariable Integer dumpFileId, HttpServletRequest request)
            throws IOException, UserAccessRightException {
        DatabaseDumpFile databaseDumpFile = this.databaseDumpFileRepo.findOne(dumpFileId);
        if (databaseDumpFile == null) {
            throw new IllegalArgumentException(String.format("Cannot find dump file with id '%s'", dumpFileId));
        }
        this.checkDatabase(databaseDumpFile.getDatabaseRef());
        String userRequest = "";
        String passwordRequest = "";
        String authorization = request.getHeader("Authorization");
        if (authorization != null && authorization.startsWith("Basic")) {
            // Authorization: Basic base64credentials
            String base64Credentials = authorization.substring("Basic".length()).trim();
            String credentials = new String(Base64.getDecoder().decode(base64Credentials),
                    Charset.forName("UTF-8"));
            // credentials = username:password
            String[] values = credentials.split(":", 2);
            userRequest = values[0];
            passwordRequest = values[1];
        } else {
            return this.getErrorResponseEntityBasicAuth();
        }
        if (!userRequest.equals(databaseDumpFile.getUser()) || !passwordRequest.equals(databaseDumpFile.getPassword())) {
            return this.getErrorResponseEntityBasicAuth();
        }
        HttpHeaders respHeaders = new HttpHeaders();
        String fileName = databaseDumpFile.getDatabaseRef().getName() + "/" + databaseDumpFile.getFileName();
        respHeaders.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        respHeaders.setContentLength(this.filer.getContentLength(fileName));
        respHeaders.setContentDispositionFormData("attachment", fileName);

        InputStream inputStream = this.filer.retrieveWithOriginalStream(fileName);

        InputStreamResource isr = new InputStreamResource(inputStream);
        return new ResponseEntity<>(isr, respHeaders, HttpStatus.OK);
    }

    private ResponseEntity<InputStreamResource> getErrorResponseEntityBasicAuth() {
        String errorMessage = "401 Unauthorized";
        HttpHeaders respHeaders = new HttpHeaders();
        respHeaders.set("WWW-Authenticate", "Basic realm=\"Download Realm\"");
        InputStream inputStream = new ByteArrayInputStream(errorMessage.getBytes());
        return new ResponseEntity<>(new InputStreamResource(inputStream), respHeaders, HttpStatus.UNAUTHORIZED);
    }
}
