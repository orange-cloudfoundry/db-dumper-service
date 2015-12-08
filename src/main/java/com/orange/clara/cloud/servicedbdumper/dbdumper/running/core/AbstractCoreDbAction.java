package com.orange.clara.cloud.servicedbdumper.dbdumper.running.core;

import com.orange.clara.cloud.servicedbdumper.dbdumper.DbDumpersFactory;
import com.orange.clara.cloud.servicedbdumper.filer.Filer;
import com.orange.clara.cloud.servicedbdumper.model.DatabaseDumpFile;
import com.orange.clara.cloud.servicedbdumper.model.DatabaseRef;
import com.orange.clara.cloud.servicedbdumper.repo.DatabaseDumpFileRepo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
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
public abstract class AbstractCoreDbAction {
    public final static String TMPFOLDER = System.getProperty("java.io.tmpdir");
    protected Logger logger = LoggerFactory.getLogger(AbstractCoreDbAction.class);
    @Autowired
    @Qualifier(value = "dbDumpersFactory")
    protected DbDumpersFactory dbDumpersFactory;
    @Autowired
    @Qualifier(value = "filer")
    protected Filer filer;
    @Autowired
    protected DatabaseDumpFileRepo databaseDumpFileRepo;
    @PersistenceContext
    protected EntityManager em;
    protected InputStream errorProcess;
    protected InputStream outputProcess;

    private String outputFromProcess = "";
    private String errorFromProcess = "";

    public static BufferedReader getOutput(Process p) {
        return new BufferedReader(new InputStreamReader(p.getInputStream()));
    }

    public static BufferedReader getError(Process p) {
        return new BufferedReader(new InputStreamReader(p.getErrorStream()));
    }

    protected Process runCommandLine(String[] commandLine) throws IOException, InterruptedException {
        logger.debug("Running command line: " + String.join(" ", commandLine));
        ProcessBuilder pb = new ProcessBuilder(commandLine);
        pb.redirectOutput(ProcessBuilder.Redirect.PIPE);
        Process process = pb.start();
        this.errorProcess = process.getErrorStream();
        this.outputProcess = process.getInputStream();
        return process;
    }

    protected void logOutputFromProcess() {
        if (!outputFromProcess.isEmpty()) {
            this.logger.error("Output from process: " + outputFromProcess);
        }
        if (!errorFromProcess.isEmpty()) {
            this.logger.error("Error from process: " + errorFromProcess);
        }
    }

    private void loadOutputsFromProcess() {
        if (outputFromProcess.isEmpty()) {
            try {
                outputFromProcess = this.getOutputFromProcess();
            } catch (IOException e) {
            }
        }
        if (errorFromProcess.isEmpty()) {
            try {
                errorFromProcess = this.getErrorFromProcess();
            } catch (IOException e) {
            }
        }
    }

    protected String getErrorMessageFromProcess() {
        this.loadOutputsFromProcess();
        String message = "";
        if (!outputFromProcess.isEmpty() || !errorFromProcess.isEmpty()) {
            message += "\nDetails: ";
        }
        if (!outputFromProcess.isEmpty()) {
            message += outputFromProcess + "\n\n";
        }
        if (!errorFromProcess.isEmpty()) {
            message += errorFromProcess;
        }
        return message;
    }

    protected String getErrorFromProcess() throws IOException {
        return this.getInputStreamToStringFromProcess(this.errorProcess);
    }

    protected String getOutputFromProcess() throws IOException {
        return this.getInputStreamToStringFromProcess(this.outputProcess);
    }

    private String getInputStreamToStringFromProcess(InputStream inputStream) throws IOException {
        String outputFromProcess = "";
        String line = "";
        if (inputStream == null) {
            return outputFromProcess;
        }
        BufferedReader brOutput = new BufferedReader(new InputStreamReader(inputStream));
        while ((line = brOutput.readLine()) != null) {
            outputFromProcess += line + "\n";
        }
        return outputFromProcess;
    }

    protected File createNewDumpFile(DatabaseRef databaseRef, String fileName) throws IOException {
        File dumpFileOutput = new File(TMPFOLDER + "/" + fileName);
        dumpFileOutput.getParentFile().mkdirs();
        dumpFileOutput.createNewFile();
        return dumpFileOutput;
    }

    protected String getFileName(DatabaseDumpFile databaseDumpFile) {
        return databaseDumpFile.getDatabaseRef().getName() + "/" + databaseDumpFile.getFileName();
    }

    protected String generateFileName() {
        Date d = new Date();
        SimpleDateFormat form = new SimpleDateFormat("dd-MM-yyyy");
        return form.format(d) + ".sql";
    }
}
