package com.orange.clara.cloud.servicedbdumper.controllers;

import com.orange.clara.cloud.servicedbdumper.config.Routes;
import com.orange.clara.cloud.servicedbdumper.dbdumper.Deleter;
import com.orange.clara.cloud.servicedbdumper.exception.DumpFileDeletedException;
import com.orange.clara.cloud.servicedbdumper.exception.DumpFileShowException;
import com.orange.clara.cloud.servicedbdumper.exception.UserAccessRightException;
import com.orange.clara.cloud.servicedbdumper.filer.Filer;
import com.orange.clara.cloud.servicedbdumper.model.DatabaseDumpFile;
import com.orange.clara.cloud.servicedbdumper.model.DatabaseRef;
import com.orange.clara.cloud.servicedbdumper.model.DbDumperServiceInstance;
import com.orange.clara.cloud.servicedbdumper.repo.DatabaseDumpFileRepo;
import com.orange.clara.cloud.servicedbdumper.security.useraccess.UserAccessRight;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

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
@RequestMapping(value = Routes.MANAGE_ROOT)
public class ManagerController {

    @Autowired
    private Filer filer;

    @Autowired
    @Qualifier("userAccessRight")
    private UserAccessRight userAccessRight;

    @Autowired
    private DatabaseDumpFileRepo databaseDumpFileRepo;

    @Autowired
    private Deleter deleter;

    @RequestMapping(Routes.RAW_DUMP_FILE_ROOT + "/{dumpFileId:[0-9]+}")
    @ResponseBody
    public String raw(@PathVariable Integer dumpFileId) throws IOException, UserAccessRightException, DumpFileShowException, DumpFileDeletedException {
        DatabaseDumpFile databaseDumpFile = getDatabaseDumpFile(dumpFileId);
        this.checkDbDumperServiceInstanceWithAccessRight(databaseDumpFile.getDbDumperServiceInstance());
        this.checkDumpShowable(databaseDumpFile);
        String databaseName = databaseDumpFile.getDbDumperServiceInstance().getDatabaseRef().getName();
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

    private void checkDbDumperServiceInstanceWithAccessRight(DbDumperServiceInstance dbDumperServiceInstance) throws UserAccessRightException {
        this.checkDbDumperServiceInstance(dbDumperServiceInstance);
        if (!this.userAccessRight.haveAccessToServiceInstance(dbDumperServiceInstance)) {
            throw new UserAccessRightException("You don't have access to this instance");
        }
    }

    private void checkDbDumperServiceInstance(DbDumperServiceInstance dbDumperServiceInstance) throws UserAccessRightException {
        if (dbDumperServiceInstance.isDeleted()) {
            throw new IllegalArgumentException(String.format("Database with name '%s' has been deleted", dbDumperServiceInstance.getDatabaseRef().getName()));
        }
    }

    private void checkDumpShowable(DatabaseDumpFile databaseDumpFile) throws DumpFileShowException, DumpFileDeletedException {
        if (!databaseDumpFile.isShowable()) {
            throw new DumpFileShowException(databaseDumpFile);
        }
        if (databaseDumpFile.isDeleted()) {
            throw new DumpFileDeletedException(databaseDumpFile);
        }
    }

    @RequestMapping(Routes.SHOW_DUMP_FILE_ROOT + "/{dumpFileId:[0-9]+}")
    public String show(@PathVariable Integer dumpFileId, Model model) throws IOException, UserAccessRightException, DumpFileShowException, DumpFileDeletedException {
        DatabaseDumpFile databaseDumpFile = getDatabaseDumpFile(dumpFileId);
        this.checkDbDumperServiceInstanceWithAccessRight(databaseDumpFile.getDbDumperServiceInstance());
        this.checkDumpShowable(databaseDumpFile);
        String databaseName = databaseDumpFile.getDbDumperServiceInstance().getDatabaseRef().getName();
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
        if (databaseDumpFile.getDbDumperServiceInstance().getDatabaseRef().getDatabaseService() != null) {
            databaseName = databaseDumpFile.getDbDumperServiceInstance().getDatabaseRef().getDatabaseService().getName();
        }
        model.addAttribute("databaseName", databaseName);
        model.addAttribute("fileName", fileName);
        model.addAttribute("id", dumpFileId);
        model.addAttribute("sql", content);
        return "show";
    }

    @RequestMapping(Routes.DELETE_DUMP_FILE_ROOT + "/{dumpFileId:[0-9]+}")
    public String delete(@PathVariable Integer dumpFileId, Model model) throws IOException, UserAccessRightException {
        DatabaseDumpFile databaseDumpFile = getDatabaseDumpFile(dumpFileId);
        this.checkDbDumperServiceInstanceWithAccessRight(databaseDumpFile.getDbDumperServiceInstance());
        DatabaseRef databaseRef = databaseDumpFile.getDbDumperServiceInstance().getDatabaseRef();
        if (!databaseDumpFile.isDeleted()) {
            this.deleter.delete(databaseDumpFile);
        }
        return String.format("redirect:" + Routes.MANAGE_ROOT + Routes.MANAGE_LIST_DATABASE_ROOT + "/%s", databaseRef.getName());
    }


    @RequestMapping(value = Routes.DOWNLOAD_DUMP_FILE_ROOT + "/{dumpFileId:[0-9]+}", method = RequestMethod.GET)
    public ResponseEntity<InputStreamResource> download(@PathVariable Integer dumpFileId, HttpServletRequest request, @RequestParam(value = "original", required = false) String original)
            throws IOException, UserAccessRightException {
        DatabaseDumpFile databaseDumpFile = getDatabaseDumpFile(dumpFileId);
        this.checkDbDumperServiceInstanceWithAccessRight(databaseDumpFile.getDbDumperServiceInstance());
        this.checkDbDumperServiceInstanceWithAccessRight(databaseDumpFile.getDbDumperServiceInstance());
        String userRequest = "";
        String passwordRequest = "";
        String authorization = request.getHeader("Authorization");
        if (authorization != null && authorization.startsWith("Basic")) {
            String base64Credentials = authorization.substring("Basic".length()).trim();
            String credentials = new String(Base64.getDecoder().decode(base64Credentials),
                    Charset.forName("UTF-8"));
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
        String fileName = databaseDumpFile.getDbDumperServiceInstance().getDatabaseRef().getName() + "/" + databaseDumpFile.getFileName();
        respHeaders.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        InputStream inputStream = null;
        if (original == null || original.isEmpty()) {
            respHeaders.setContentLength(this.filer.getContentLength(fileName));
            inputStream = this.filer.retrieveWithOriginalStream(fileName);
        } else {
            inputStream = this.filer.retrieveWithStream(fileName);
            File file = new File(fileName);
            String[] filenames = file.getName().split("\\.");
            if (filenames.length >= 2) {
                fileName = filenames[0] + "." + filenames[1];
            }

        }
        File file = new File(fileName);
        respHeaders.setContentDispositionFormData("attachment", file.getName());
        InputStreamResource isr = new InputStreamResource(inputStream);
        return new ResponseEntity<>(isr, respHeaders, HttpStatus.OK);
    }

    private DatabaseDumpFile getDatabaseDumpFile(Integer dumpFileId) {
        DatabaseDumpFile databaseDumpFile = this.databaseDumpFileRepo.findOne(dumpFileId);
        if (databaseDumpFile == null) {
            throw new IllegalArgumentException(String.format("Cannot find dump file with id '%s'", dumpFileId));
        }
        return databaseDumpFile;
    }

    private ResponseEntity<InputStreamResource> getErrorResponseEntityBasicAuth() {
        String errorMessage = "401 Unauthorized";
        HttpHeaders respHeaders = new HttpHeaders();
        respHeaders.set("WWW-Authenticate", "Basic realm=\"Download Realm\"");
        InputStream inputStream = new ByteArrayInputStream(errorMessage.getBytes());
        return new ResponseEntity<>(new InputStreamResource(inputStream), respHeaders, HttpStatus.UNAUTHORIZED);
    }
}
