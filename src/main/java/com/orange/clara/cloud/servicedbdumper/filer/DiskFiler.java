package com.orange.clara.cloud.servicedbdumper.filer;

import com.google.common.io.ByteStreams;
import com.google.common.io.Files;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;

/**
 * Copyright (C) 2016 Orange
 * <p>
 * This software is distributed under the terms and conditions of the 'Apache-2.0'
 * license which can be found in the file 'LICENSE' in this package distribution
 * or at 'https://opensource.org/licenses/Apache-2.0'.
 * <p>
 * Author: Arthur Halet
 * Date: 10/02/2016
 */
public class DiskFiler implements Filer {
    public final static String TMPFOLDER = System.getProperty("java.io.tmpdir");
    private Logger logger = LoggerFactory.getLogger(DiskFiler.class);

    private File createNewDumpFile(File dumpFileOutput) throws IOException {
        dumpFileOutput.getParentFile().mkdirs();
        dumpFileOutput.createNewFile();
        return dumpFileOutput;
    }

    protected File getFile(String filename) {
        return new File(TMPFOLDER + "/" + filename);
    }

    @Override
    public void store(InputStream inputStream, String filename) throws IOException {
        File dumpfile = this.getFile(filename);
        logger.debug("Storing in file " + dumpfile.getAbsolutePath());
        this.createNewDumpFile(dumpfile);
        FileOutputStream dumpFileOutputStream = new FileOutputStream(dumpfile);
        ByteStreams.copy(inputStream, dumpFileOutputStream);
        dumpFileOutputStream.flush();
        dumpFileOutputStream.close();
        inputStream.close();
        logger.debug("Store finished in file " + dumpfile.getAbsolutePath());
    }

    @Override
    public void retrieve(OutputStream outputStream, String filename) throws IOException {
        File dumpfile = this.getFile(filename);
        logger.debug("Retrieving from file " + dumpfile.getAbsolutePath());
        InputStream dumpFileInputStream = Files.asByteSource(dumpfile).openStream();
        ByteStreams.copy(dumpFileInputStream, outputStream);
        outputStream.flush();
        dumpFileInputStream.close();
        outputStream.close();
        logger.debug("Retrieve finished file " + dumpfile.getAbsolutePath());
    }

    @Override
    public InputStream retrieveWithStream(String filename) throws IOException {
        File dumpfile = this.getFile(filename);
        return Files.asByteSource(dumpfile).openStream();
    }

    @Override
    public InputStream retrieveWithOriginalStream(String filename) throws IOException {
        File dumpfile = this.getFile(filename);
        return Files.asByteSource(dumpfile).openStream();
    }

    @Override
    public void delete(String filename) {
        File dumpfile = this.getFile(filename);
        dumpfile.delete();
        logger.debug("File: '" + dumpfile.getAbsolutePath() + "' deleted.");
    }

    @Override
    public long getContentLength(String filename) {
        File dumpfile = this.getFile(filename);
        return dumpfile.length();
    }

    @Override
    public boolean exists(String filename) {
        return this.getFile(filename).exists();
    }

    @Override
    public String getAppendedFileExtension() {
        return "";
    }
}
