package com.orange.clara.cloud.dbdump.action;

import com.orange.clara.cloud.dbdump.DbDumpersFactory;
import com.orange.clara.cloud.model.DatabaseRef;
import com.orange.clara.cloud.repo.DatabaseDumpFileRepo;
import org.jclouds.blobstore.BlobStoreContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Copyright (C) 2015 Orange
 * <p/>
 * This software is distributed under the terms and conditions of the 'MIT'
 * license which can be found in the file 'LICENSE' in this package distribution
 * or at 'http://opensource.org/licenses/MIT'.
 * <p/>
 * Author: Arthur Halet
 * Date: 03/06/2015
 */
public abstract class AbstractDbAction {
    public final static String TMPFOLDER = System.getProperty("java.io.tmpdir");

    @Autowired
    @Qualifier(value = "dbDumpersFactory")
    protected DbDumpersFactory dbDumpersFactory;

    @Autowired
    @Qualifier(value = "bucketName")
    protected String bucketName;

    @Autowired
    protected DatabaseDumpFileRepo databaseDumpFileRepo;

    @Autowired
    @Qualifier(value = "blobStoreContext")
    protected BlobStoreContext blobStoreContext;

    protected static BufferedReader getOutput(Process p) {
        return new BufferedReader(new InputStreamReader(p.getInputStream()));
    }

    protected static BufferedReader getError(Process p) {
        return new BufferedReader(new InputStreamReader(p.getErrorStream()));
    }

    protected String runCommandLine(String[] commandLine) throws IOException, InterruptedException {
        Process p = Runtime.getRuntime().exec(commandLine);
        BufferedReader output = getOutput(p);
        BufferedReader error = getError(p);
        String outputLine = "";
        String line = "";

        while ((line = output.readLine()) != null) {
            outputLine += line;
        }

        while ((line = error.readLine()) != null) {
            outputLine += line;
        }
        p.waitFor();
        return outputLine;
    }

    protected String streamToString(InputStream in) throws IOException {
        StringBuilder out = new StringBuilder();
        BufferedReader br = new BufferedReader(new InputStreamReader(in));
        for (String line = br.readLine(); line != null; line = br.readLine()) {
            out.append(line);
            out.append("\n");
        }
        br.close();
        return out.toString();
    }

    protected File createNewDumpFile(DatabaseRef databaseRef, String fileName) throws IOException {
        File dumpFileOutput = new File(TMPFOLDER + "/" + fileName);
        dumpFileOutput.getParentFile().mkdirs();
        dumpFileOutput.createNewFile();
        return dumpFileOutput;
    }

    protected String getFileName(DatabaseRef databaseRef) {
        Date d = new Date();
        SimpleDateFormat form = new SimpleDateFormat("dd-MM-yyyy");
        return databaseRef.getName() + "/" + form.format(d) + ".sql";
    }

    abstract public String action(DatabaseRef databaseRef) throws IOException, InterruptedException;
}
