package com.orange.clara.cloud.servicedbdumper.dbdump.action;

import com.orange.clara.cloud.servicedbdumper.dbdump.DatabaseDumper;
import com.orange.clara.cloud.servicedbdumper.model.DatabaseDumpFile;
import com.orange.clara.cloud.servicedbdumper.model.DatabaseRef;
import org.slf4j.LoggerFactory;
import org.jclouds.blobstore.BlobStore;
import org.jclouds.blobstore.domain.Blob;
import org.slf4j.Logger;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Copyright (C) 2015 Orange
 * <p>
 * This software is distributed under the terms and conditions of the 'MIT'
 * license which can be found in the file 'LICENSE' in this package distribution
 * or at 'http://opensource.org/licenses/MIT'.
 * <p>
 * Author: Arthur Halet
 * Date: 09/09/2015
 */
public class Dumper extends AbstractDbAction {

    private Logger logger = LoggerFactory.getLogger(Dumper.class);

    public void dump(DatabaseRef databaseRef) throws IOException, InterruptedException {
        String fileName = this.getFileName(databaseRef);

        DatabaseDumper databaseDumper = dbDumpersFactory.getDatabaseDumper(databaseRef);

        logger.info("Dumping database '" + databaseRef.getName() + "' with " + databaseRef.getType() + " binary.");

        Process p = this.runCommandLine(databaseDumper.getDumpCommandLine());

        BlobStore blobStore = this.blobStoreContext.getBlobStore();
        Blob blob = blobStore.blobBuilder(fileName).build();
        logger.info("Uploading dump file '" + fileName + "'  on S3 storage.");
        this.uploadS3Stream.upload(p.getInputStream(), blob);
        p.getInputStream().close();

        SimpleDateFormat form = new SimpleDateFormat("dd-MM-yyyy");
        Date today = new Date();
        try {
            today = form.parse(form.format(new Date()));
        } catch (ParseException e) { // should have no error
        }
        if (this.databaseDumpFileRepo.findByDatabaseRefAndCreatedAt(databaseRef, today) == null) {
            this.databaseDumpFileRepo.save(new DatabaseDumpFile(fileName, databaseRef));
        }
    }

}
