package com.orange.clara.cloud.dbdump.action;

import com.google.common.base.Joiner;
import com.google.common.io.Files;
import com.orange.clara.cloud.cloudfoundry.RiakcsContextBuilder;
import com.orange.clara.cloud.dbdump.DatabaseDumper;
import com.orange.clara.cloud.dbdump.DbDumpersFactory;
import com.orange.clara.cloud.model.DatabaseDumpFile;
import com.orange.clara.cloud.model.DatabaseRef;
import com.orange.clara.cloud.repo.DatabaseDumpFileRepo;
import org.jclouds.blobstore.BlobStore;
import org.jclouds.blobstore.BlobStoreContext;
import org.jclouds.blobstore.domain.Blob;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
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

    @Autowired
    @Qualifier(value = "riakcsContextBuilderBean")
    private RiakcsContextBuilder riakcsContextBuilder;

    @Autowired
    private DatabaseDumpFileRepo databaseDumpFileRepo;

    @Autowired
    @Qualifier(value = "blobStoreContext")
    private BlobStoreContext blobStoreContext;

    private static BufferedReader getOutput(Process p) {
        return new BufferedReader(new InputStreamReader(p.getInputStream()));
    }

    private static BufferedReader getError(Process p) {
        return new BufferedReader(new InputStreamReader(p.getErrorStream()));
    }

    public String dump(DatabaseRef databaseRef) throws IOException, InterruptedException {
        String fileName = this.getFileName(databaseRef);
        File dumpFileOutput = this.createNewDumpFile(databaseRef, fileName);

        DatabaseDumper databaseDumper = dbDumpersFactory.getDatabaseDumper(databaseRef.getType());
        databaseDumper.setDatabaseRef(databaseRef);
        String ouputCommand = this.runCommandLine(databaseDumper.getDumpCommandLine(dumpFileOutput.getAbsolutePath()));

        BlobStore blobStore = this.blobStoreContext.getBlobStore();
        Blob blob = blobStore.blobBuilder(fileName).build();
        blob.setPayload(dumpFileOutput);
        blobStore.putBlob(this.riakcsContextBuilder.getBucketName(), blob);

        blob = blobStore.getBlob(this.riakcsContextBuilder.getBucketName(), fileName);
        InputStream inputStream = blob.getPayload().openStream();

        List<String> lines = new ArrayList<String>();
        lines.add(this.streamToString(inputStream));
        inputStream.close();
        lines.add(ouputCommand);

        this.databaseDumpFileRepo.save(new DatabaseDumpFile(dumpFileOutput, databaseRef));

        dumpFileOutput.delete();

        return Joiner.on("\n").join(lines);
    }

    private String streamToString(InputStream in) throws IOException {
        StringBuilder out = new StringBuilder();
        BufferedReader br = new BufferedReader(new InputStreamReader(in));
        for (String line = br.readLine(); line != null; line = br.readLine()) {
            out.append(line);
            out.append("\n");
        }
        br.close();
        return out.toString();
    }

    private File createNewDumpFile(DatabaseRef databaseRef, String fileName) throws IOException {
        File dumpFileOutput = new File(TMPFOLDER + "/" + fileName);
        dumpFileOutput.getParentFile().mkdirs();
        dumpFileOutput.createNewFile();
        return dumpFileOutput;
    }

    private String getFileName(DatabaseRef databaseRef) {
        Date d = new Date();
        SimpleDateFormat form = new SimpleDateFormat("dd-mm-yyyy_hhmmss");
        return databaseRef.getName() + "/" + form.format(d) + ".sql";
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
