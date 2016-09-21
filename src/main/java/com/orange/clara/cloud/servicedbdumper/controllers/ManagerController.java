package com.orange.clara.cloud.servicedbdumper.controllers;

import com.google.common.io.ByteStreams;
import com.google.common.io.Closeables;
import com.orange.clara.cloud.servicedbdumper.config.Routes;
import com.orange.clara.cloud.servicedbdumper.dbdumper.Deleter;
import com.orange.clara.cloud.servicedbdumper.exception.DumpFileDeletedException;
import com.orange.clara.cloud.servicedbdumper.exception.DumpFileShowException;
import com.orange.clara.cloud.servicedbdumper.exception.UserAccessRightException;
import com.orange.clara.cloud.servicedbdumper.filer.Filer;
import com.orange.clara.cloud.servicedbdumper.helper.DumpFileHelper;
import com.orange.clara.cloud.servicedbdumper.model.DatabaseDumpFile;
import com.orange.clara.cloud.servicedbdumper.model.DatabaseRef;
import com.orange.clara.cloud.servicedbdumper.model.DbDumperServiceInstance;
import com.orange.clara.cloud.servicedbdumper.repo.DatabaseDumpFileRepo;
import com.orange.clara.cloud.servicedbdumper.security.useraccess.UserAccessRight;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
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
public class ManagerController extends AbstractController {
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
        String fileName = DumpFileHelper.getFilePath(databaseDumpFile);
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

    private void checkDbDumperServiceInstance(DbDumperServiceInstance dbDumperServiceInstance) {
        if (dbDumperServiceInstance.isDeleted()) {
            throw new IllegalArgumentException(String.format("Instance with for database reference '%s' has been deleted", dbDumperServiceInstance.getDatabaseRef().getName()));
        }
    }

    private void checkDatabaseDumpFile(DatabaseDumpFile databaseDumpFile) throws DumpFileDeletedException {
        if (databaseDumpFile.isDeleted()) {
            throw new DumpFileDeletedException(databaseDumpFile);
        }
    }

    private void checkDumpShowable(DatabaseDumpFile databaseDumpFile) throws DumpFileShowException, DumpFileDeletedException {
        if (!databaseDumpFile.isShowable()) {
            throw new DumpFileShowException(databaseDumpFile);
        }
        this.checkDatabaseDumpFile(databaseDumpFile);
    }

    @RequestMapping(Routes.SHOW_DUMP_FILE_ROOT + "/{dumpFileId:[0-9]+}")
    public String show(@PathVariable Integer dumpFileId, Model model) throws IOException, UserAccessRightException, DumpFileShowException, DumpFileDeletedException {
        DatabaseDumpFile databaseDumpFile = getDatabaseDumpFile(dumpFileId);
        this.checkDbDumperServiceInstanceWithAccessRight(databaseDumpFile.getDbDumperServiceInstance());
        this.checkDumpShowable(databaseDumpFile);
        String databaseName = databaseDumpFile.getDbDumperServiceInstance().getDatabaseRef().getName();
        String fileName = databaseDumpFile.getFileName();
        String finalFileName = DumpFileHelper.getFilePath(databaseDumpFile);
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
        this.addDefaultAttribute(model);
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
    public void download(@PathVariable Integer dumpFileId, HttpServletRequest request, HttpServletResponse resp, @RequestParam(value = "original", required = false) String original)
            throws IOException, DumpFileDeletedException {
        DatabaseDumpFile databaseDumpFile = getDatabaseDumpFile(dumpFileId);
        this.checkDbDumperServiceInstance(databaseDumpFile.getDbDumperServiceInstance());
        this.checkDatabaseDumpFile(databaseDumpFile);
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
            this.getErrorResponseEntityBasicAuth(resp);
            return;
        }

        if (!userRequest.equals(databaseDumpFile.getUser()) || !passwordRequest.equals(databaseDumpFile.getPassword())) {
            this.getErrorResponseEntityBasicAuth(resp);
            return;
        }

        String fileName = DumpFileHelper.getFilePath(databaseDumpFile);
        resp.setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE);
        resp.setHeader(HttpHeaders.CONTENT_LENGTH, Long.toString(this.filer.getContentLength(fileName)));
        InputStream inputStream = null;
        if (original == null || original.isEmpty()) {
            inputStream = filer.retrieveWithOriginalStream(fileName);
        } else {
            inputStream = filer.retrieveWithStream(fileName);
            File file = new File(fileName);
            String[] filenames = file.getName().split("\\.");
            if (filenames.length >= 2) {
                fileName = filenames[0] + "." + filenames[1];
            }

        }
        File file = new File(fileName);
        resp.setHeader("Content-Disposition", "attachment; filename=\"" + file.getName() + "\"");

        OutputStream outputStream = null;
        outputStream = resp.getOutputStream();
        try {
            ByteStreams.copy(inputStream, outputStream);
        } finally {
            Closeables.closeQuietly(inputStream);
            Closeables.close(outputStream, true);
        }
    }

    private DatabaseDumpFile getDatabaseDumpFile(Integer dumpFileId) {
        DatabaseDumpFile databaseDumpFile = this.databaseDumpFileRepo.findOne(dumpFileId);
        if (databaseDumpFile == null) {
            throw new IllegalArgumentException(String.format("Cannot find dump file with id '%s'", dumpFileId));
        }
        return databaseDumpFile;
    }

    private void getErrorResponseEntityBasicAuth(HttpServletResponse resp) throws IOException {
        String errorMessage = "401 Unauthorized";

        resp.setHeader("WWW-Authenticate", "Basic realm=\"Download Realm\"");
        resp.setStatus(HttpStatus.UNAUTHORIZED.value());
        OutputStream outputStream = resp.getOutputStream();
        try {
            outputStream.write(errorMessage.getBytes());
        } finally {
            Closeables.close(outputStream, true);
        }

    }
}
