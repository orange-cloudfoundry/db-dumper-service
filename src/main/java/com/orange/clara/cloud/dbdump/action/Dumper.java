package com.orange.clara.cloud.dbdump.action;

import com.google.common.base.Joiner;
import com.orange.clara.cloud.dbdump.DatabaseDumper;
import com.orange.clara.cloud.model.DatabaseDumpFile;
import com.orange.clara.cloud.model.DatabaseRef;
import org.jclouds.blobstore.BlobStore;
import org.jclouds.blobstore.domain.Blob;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
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

    public String action(DatabaseRef databaseRef) throws IOException, InterruptedException {
        String fileName = this.getFileName(databaseRef);
        File dumpFileOutput = this.createNewDumpFile(databaseRef, fileName);

        DatabaseDumper databaseDumper = dbDumpersFactory.getDatabaseDumper(databaseRef.getType());
        databaseDumper.setDatabaseRef(databaseRef);

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

        this.databaseDumpFileRepo.save(new DatabaseDumpFile(dumpFileOutput, databaseRef));

        dumpFileOutput.delete();

        return Joiner.on("\n").join(lines);
    }

}
