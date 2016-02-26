package com.orange.clara.cloud.servicedbdumper.dbdumper.core;

import com.orange.clara.cloud.servicedbdumper.dbdumper.core.dbdrivers.DatabaseDriver;
import com.orange.clara.cloud.servicedbdumper.dbdumper.core.dbdrivers.DbDumpersFactory;
import com.orange.clara.cloud.servicedbdumper.filer.Filer;
import com.orange.clara.cloud.servicedbdumper.model.DatabaseDumpFile;
import com.orange.clara.cloud.servicedbdumper.repo.DatabaseDumpFileRepo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Copyright (C) 2015 Orange
 * <p>
 * This software is distributed under the terms and conditions of the 'Apache-2.0'
 * license which can be found in the file 'LICENSE' in this package distribution
 * or at 'https://opensource.org/licenses/Apache-2.0'.
 * <p>
 * Author: Arthur Halet
 * Date: 03/06/2015
 */
public abstract class AbstractCoreDbAction {
    protected Logger logger = LoggerFactory.getLogger(AbstractCoreDbAction.class);

    @Autowired
    @Qualifier(value = "dateFormatFile")
    protected String dateFormat;


    @Autowired
    @Qualifier(value = "dbDumpersFactory")
    protected DbDumpersFactory dbDumpersFactory;

    @Autowired
    @Qualifier(value = "filer")
    protected Filer filer;

    @Autowired
    protected DatabaseDumpFileRepo databaseDumpFileRepo;

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

    protected String getFileName(DatabaseDumpFile databaseDumpFile) {
        return databaseDumpFile.getDatabaseRef().getName() + "/" + databaseDumpFile.getFileName();
    }

    protected String generateFileName(DatabaseDriver databaseDriver) {
        Date d = new Date();
        SimpleDateFormat form = new SimpleDateFormat(this.dateFormat);
        return form.format(d) + databaseDriver.getFileExtension() + this.filer.getAppendedFileExtension();
    }
}
