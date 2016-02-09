package com.orange.clara.cloud.servicedbdumper.filer;

import com.orange.clara.cloud.servicedbdumper.filer.compression.GzipCompressing;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.*;

/**
 * Copyright (C) 2016 Arthur Halet
 * <p/>
 * This software is distributed under the terms and conditions of the 'MIT'
 * license which can be found in the file 'LICENSE' in this package distribution
 * or at 'http://opensource.org/licenses/MIT'.
 * <p/>
 * Author: Arthur Halet
 * Date: 09/02/2016
 */
public class GzipS3Filer extends S3Filer implements Filer {
    private Logger logger = LoggerFactory.getLogger(GzipS3Filer.class);

    @Autowired
    private GzipCompressing gzipCompressing;

    @Override
    public void store(InputStream inputStream, String filename) throws IOException {
        PipedOutputStream outputPipe = new PipedOutputStream();
        PipedInputStream inputPipe = new PipedInputStream(outputPipe);
        gzipCompressing.gziptIt(inputStream, outputPipe);
        logger.debug("Gzip file ...");
        super.store(inputPipe, filename);
    }

    @Override
    public void retrieve(OutputStream outputStream, String filename) throws IOException {
        PipedOutputStream outputPipe = new PipedOutputStream();
        PipedInputStream inputPipe = new PipedInputStream(outputPipe);
        gzipCompressing.gunziptIt(outputStream, inputPipe);
        super.retrieve(outputPipe, filename);
    }

    @Override
    public InputStream retrieveWithStream(String filename) throws IOException {
        PipedOutputStream outputPipe = new PipedOutputStream();
        PipedInputStream inputPipe = new PipedInputStream(outputPipe);

        InputStream inputStream = super.retrieveWithStream(filename);
        gzipCompressing.gunziptIt(outputPipe, inputStream);
        return inputPipe;
    }

    @Override
    public InputStream retrieveWithOriginalStream(String filename) throws IOException {
        return super.retrieveWithOriginalStream(filename);
    }

    @Override
    public String getFileExtension() {
        return ".sql.gzip";
    }
}
