package com.orange.clara.cloud.servicedbdumper.dbdump.action;

import com.google.common.io.ByteStreams;
import com.orange.clara.cloud.servicedbdumper.dbdump.DatabaseDumper;
import com.orange.clara.cloud.servicedbdumper.exception.RestoreCannotFindFile;
import com.orange.clara.cloud.servicedbdumper.exception.RestoreException;
import com.orange.clara.cloud.servicedbdumper.model.DatabaseDumpFile;
import com.orange.clara.cloud.servicedbdumper.model.DatabaseRef;
import org.jclouds.blobstore.BlobStore;
import org.jclouds.blobstore.domain.Blob;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;

/**
 * Copyright (C) 2015 Orange
 * <p>
 * This software is distributed under the terms and conditions of the 'MIT'
 * license which can be found in the file 'LICENSE' in this package distribution
 * or at 'http://opensource.org/licenses/MIT'.
 * <p>
 * Author: Arthur Halet
 * Date: 01/10/2015
 */
public class Restorer extends AbstractDbAction {

    private Logger logger = LoggerFactory.getLogger(Restorer.class);

    public void restore(DatabaseRef databaseRefSource, DatabaseRef databaseRefTarget, Date date) throws RestoreException {
        logger.info("Restoring dump file from " + databaseRefSource.getName() + " to " + databaseRefTarget.getName());
        DatabaseDumpFile dumpFile;
        if (date == null) {
            dumpFile = this.databaseDumpFileRepo.findFirstByDatabaseRefOrderByCreatedAtDesc(databaseRefSource);
        } else {
            dumpFile = this.databaseDumpFileRepo.findByDatabaseRefAndCreatedAt(databaseRefSource, date);
        }
        if (dumpFile == null) {
            throw new RestoreCannotFindFile(databaseRefSource);
        }
        String fileName = databaseRefSource.getName() + "/" + dumpFile.getFileName();

        DatabaseDumper databaseDumper = dbDumpersFactory.getDatabaseDumper(databaseRefTarget);
        try {
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
        } catch (Exception e) {
            throw new RestoreException("An error occurred: " + e.getMessage(), e);
        }
    }

    public void restore(DatabaseRef databaseRefSource, DatabaseRef databaseRefTarget) throws RestoreException {
        this.restore(databaseRefSource, databaseRefTarget, null);
    }
}
