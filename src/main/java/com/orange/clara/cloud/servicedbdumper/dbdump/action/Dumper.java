package com.orange.clara.cloud.servicedbdumper.dbdump.action;

import com.google.common.base.Joiner;
import com.orange.clara.cloud.servicedbdumper.dbdump.DatabaseDumper;
import com.orange.clara.cloud.servicedbdumper.model.DatabaseDumpFile;
import com.orange.clara.cloud.servicedbdumper.model.DatabaseRef;
import org.jclouds.blobstore.BlobStore;
import org.jclouds.blobstore.domain.Blob;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Copyright (C) 2015 Orange
 * <p/>
 * This software is distributed under the terms and conditions of the 'MIT'
 * license which can be found in the file 'LICENSE' in this package distribution
 * or at 'http://opensource.org/licenses/MIT'.
 * <p/>
 * Author: Arthur Halet
 * Date: 09/09/2015
 */
public class Dumper extends AbstractDbAction {

    public String dump(DatabaseRef databaseRef) throws IOException, InterruptedException {
        String fileName = this.getFileName(databaseRef);
        File dumpFileOutput = this.createNewDumpFile(databaseRef, fileName);

        DatabaseDumper databaseDumper = dbDumpersFactory.getDatabaseDumper(databaseRef);

        Process p = this.runCommandLine(databaseDumper.getDumpCommandLine());

        BlobStore blobStore = this.blobStoreContext.getBlobStore();
        Blob blob = blobStore.blobBuilder(fileName).build();
        this.uploadS3Stream.upload(p.getInputStream(), blob);
        p.getInputStream().close();

        blob = blobStore.getBlob(this.bucketName, fileName);
        InputStream inputStream = blob.getPayload().openStream();

        List<String> lines = new ArrayList<>();
        lines.add(this.streamToString(inputStream));
        inputStream.close();
        SimpleDateFormat form = new SimpleDateFormat("dd-MM-yyyy");
        Date today = new Date();
        try {
            today = form.parse(form.format(new Date()));
        } catch (ParseException e) { // should have no error
        }
        if (this.databaseDumpFileRepo.findByDatabaseRefAndCreatedAt(databaseRef, today) == null) {
            this.databaseDumpFileRepo.save(new DatabaseDumpFile(dumpFileOutput, databaseRef));
        }
        dumpFileOutput.delete();

        return Joiner.on("\n").join(lines);
    }

}
