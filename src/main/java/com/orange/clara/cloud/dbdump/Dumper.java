package com.orange.clara.cloud.dbdump;

import com.google.common.base.Joiner;
import com.google.common.io.Files;
import com.orange.clara.cloud.model.DatabaseRef;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * Copyright (C) 2015 Arthur Halet
 * <p/>
 * This software is distributed under the terms and conditions of the 'MIT'
 * license which can be found in the file 'LICENSE' in this package distribution
 * or at 'http://opensource.org/licenses/MIT'.
 * <p/>
 * Author: Arthur Halet
 * Date: 09/09/2015
 */
public class Dumper {
    public final static String TMPFOLDER = System.getProperty("java.io.tmpdir");

    @Autowired
    @Qualifier(value = "dbDumpersFactory")
    private DbDumpersFactory dbDumpersFactory;

    private static BufferedReader getOutput(Process p) {
        return new BufferedReader(new InputStreamReader(p.getInputStream()));
    }

    private static BufferedReader getError(Process p) {
        return new BufferedReader(new InputStreamReader(p.getErrorStream()));
    }

    public String dump(DatabaseRef databaseRef) throws IOException, InterruptedException {
        File dumpFileOutput = this.createNewDumpFile(databaseRef);

        DatabaseDumper databaseDumper = dbDumpersFactory.getDatabaseDumper(databaseRef.getType());
        databaseDumper.setDatabaseRef(databaseRef);
        String ouputCommand = this.runCommandLine(databaseDumper.getDumpCommandLine(dumpFileOutput.getAbsolutePath()));
        List<String> lines = Files.readLines(dumpFileOutput, StandardCharsets.UTF_8);
        lines.add(ouputCommand);
        return Joiner.on("\n").join(lines);
    }

    private File createNewDumpFile(DatabaseRef databaseRef) throws IOException {
        Date d = new Date();
        SimpleDateFormat form = new SimpleDateFormat("dd-mm-yyyy_hhmmss");
        File dumpFileOutput = new File(TMPFOLDER + "/" + databaseRef.getName() + "/" + form.format(d) + ".sql");
        dumpFileOutput.getParentFile().mkdirs();
        dumpFileOutput.createNewFile();
        return dumpFileOutput;
    }

    private String runCommandLine(String[] commandLine) throws IOException, InterruptedException {
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
}
