package com.orange.clara.cloud.dbdump.action;

import com.google.common.io.ByteStreams;
import com.orange.clara.cloud.dbdump.DatabaseDumper;
import com.orange.clara.cloud.model.DatabaseDumpFile;
import com.orange.clara.cloud.model.DatabaseRef;
import org.jclouds.blobstore.BlobStore;
import org.jclouds.blobstore.domain.Blob;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Copyright (C) 2015 Orange
 * <p/>
 * This software is distributed under the terms and conditions of the 'MIT'
 * license which can be found in the file 'LICENSE' in this package distribution
 * or at 'http://opensource.org/licenses/MIT'.
 * <p/>
 * Author: Arthur Halet
 * Date: 01/10/2015
 */
public class Restorer extends AbstractDbAction {

    public String restore(DatabaseRef databaseRefSource, DatabaseRef databaseRefTarget) throws IOException, InterruptedException {
        DatabaseDumpFile dumpFile = this.databaseDumpFileRepo.findByDatabaseRefOrderByCreatedAtDesc(databaseRefSource);

        String fileName = databaseRefSource.getName() + "/" + dumpFile.getFileName();

        DatabaseDumper databaseDumper = dbDumpersFactory.getDatabaseDumper(databaseRefTarget);
        Process p = this.runCommandLine(databaseDumper.getRestoreCommandLine());
        OutputStream outputStream = p.getOutputStream();
        BlobStore blobStore = this.blobStoreContext.getBlobStore();
        Blob blob = blobStore.getBlob(this.bucketName, fileName);
        InputStream inputStream = blob.getPayload().openStream();
        ByteStreams.copy(inputStream, outputStream);
        outputStream.flush();
        inputStream.close();
        outputStream.close();
        p.waitFor();
        return "restored";
    }
}
