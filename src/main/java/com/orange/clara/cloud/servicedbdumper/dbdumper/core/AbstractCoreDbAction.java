package com.orange.clara.cloud.servicedbdumper.dbdumper.core;

import com.orange.clara.cloud.servicedbdumper.dbdumper.core.dbdrivers.DatabaseDriver;
import com.orange.clara.cloud.servicedbdumper.dbdumper.core.dbdrivers.DbDumpersFactory;
import com.orange.clara.cloud.servicedbdumper.filer.Filer;
import com.orange.clara.cloud.servicedbdumper.helper.DumpFileHelper;
import com.orange.clara.cloud.servicedbdumper.model.DatabaseDumpFile;
import com.orange.clara.cloud.servicedbdumper.model.DatabaseRef;
import com.orange.clara.cloud.servicedbdumper.repo.DatabaseDumpFileRepo;
import com.orange.clara.cloud.servicedbdumper.repo.DbDumperServiceInstanceRepo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;

import java.io.BufferedReader;
import java.io.IOException;
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

    @Value("${db.command.retry:5}")
    protected int dbCommandRetry = 5;

    @Autowired
    @Qualifier(value = "dbDumpersFactory")
    protected DbDumpersFactory dbDumpersFactory;

    @Autowired
    protected Filer filer;

    @Autowired
    protected DatabaseDumpFileRepo databaseDumpFileRepo;

    @Autowired
    protected DbDumperServiceInstanceRepo serviceInstanceRepo;


    @Value("${show.command.line:true}")
    private boolean showCommandLine;

    private String outputFromProcess = "";
    private String errorFromProcess = "";

    public static BufferedReader getOutput(Process p) {
        return new BufferedReader(new InputStreamReader(p.getInputStream()));
    }

    public static BufferedReader getError(Process p) {
        return new BufferedReader(new InputStreamReader(p.getErrorStream()));
    }

    private ProcessBuilder generateProcessBuilder(String[] commandLine) {
        if (this.showCommandLine) {
            logger.info("Running command line: " + String.join(" ", commandLine));
        }
        ProcessBuilder pb = new ProcessBuilder(commandLine);
        return pb;
    }

    protected Process runCommandLine(String[] commandLine) throws IOException, InterruptedException {
        return this.runCommandLine(commandLine, false);
    }

    protected Process runCommandLine(String[] commandLine, boolean inInheritOutput) throws IOException, InterruptedException {
        ProcessBuilder pb = this.generateProcessBuilder(commandLine);
        if (inInheritOutput) {
            pb.redirectOutput(ProcessBuilder.Redirect.INHERIT);
        } else {
            pb.redirectOutput(ProcessBuilder.Redirect.PIPE);
        }
        pb.redirectError(ProcessBuilder.Redirect.INHERIT);
        Process process = pb.start();

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


    protected String getFileName(DatabaseDumpFile databaseDumpFile) {
        return DumpFileHelper.getFilePath(databaseDumpFile);
    }

    protected String generateFileName(DatabaseRef databaseRef, DatabaseDriver databaseDriver) {
        Date d = new Date();
        SimpleDateFormat form = new SimpleDateFormat(this.dateFormat);
        String filename = form.format(d) + databaseDriver.getFileExtension() + this.filer.getAppendedFileExtension();
        int i = 1;
        while (this.filer.exists(DumpFileHelper.getFilePath(databaseRef, filename))) {
            filename = form.format(d) + "(" + i + ")" + databaseDriver.getFileExtension() + this.filer.getAppendedFileExtension();
            i++;
        }
        return filename;
    }
}
