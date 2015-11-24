package com.orange.clara.cloud.servicedbdumper.dbdumper.running.core;

import com.orange.clara.cloud.servicedbdumper.dbdumper.DbDumpersFactory;
import com.orange.clara.cloud.servicedbdumper.filer.Filer;
import com.orange.clara.cloud.servicedbdumper.model.DatabaseRef;
import com.orange.clara.cloud.servicedbdumper.repo.DatabaseDumpFileRepo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
        return pb.start();
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

    protected String createFileName(DatabaseRef databaseRef) {
        Date d = new Date();
        SimpleDateFormat form = new SimpleDateFormat("dd-MM-yyyy");
        return databaseRef.getName() + "/" + form.format(d) + ".sql";
    }
}
