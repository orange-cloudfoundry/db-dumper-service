package com.orange.clara.cloud.servicedbdumper.filer;

import com.orange.clara.cloud.servicedbdumper.filer.compression.GzipCompressing;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

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
abstract public class AbstractGzipGenericFiler implements Filer {
    @Autowired
    protected GzipCompressing gzipCompressing;
    protected Filer originalFiler;

    private Logger logger = LoggerFactory.getLogger(AbstractGzipGenericFiler.class);

    public AbstractGzipGenericFiler(Filer originalFiler) {
        this.originalFiler = originalFiler;
    }

    @Override
    public void store(InputStream inputStream, String filename) throws IOException {
        PipedOutputStream outputPipe = new PipedOutputStream();
        PipedInputStream inputPipe = new PipedInputStream(outputPipe);
        gzipCompressing.gziptIt(inputStream, outputPipe);
        logger.debug("Gziping file ...");
        this.originalFiler.store(inputPipe, filename);
    }

    @Override
    public void retrieve(OutputStream outputStream, String filename) throws IOException {
        InputStream inputStream = this.originalFiler.retrieveWithStream(filename);
        gzipCompressing.gunziptIt(outputStream, inputStream);
        logger.debug("Gunziping file ...");
    }

    @Override
    public InputStream retrieveWithStream(String filename) throws IOException {
        PipedOutputStream outputPipe = new PipedOutputStream();
        PipedInputStream inputPipe = new PipedInputStream(outputPipe);

        InputStream inputStream = this.originalFiler.retrieveWithStream(filename);
        gzipCompressing.gunziptIt(outputPipe, inputStream);
        logger.debug("Gunziping file ...");
        return inputPipe;
    }

    @Override
    public InputStream retrieveWithOriginalStream(String filename) throws IOException {
        return this.originalFiler.retrieveWithOriginalStream(filename);
    }

    @Override
    public void delete(String filename) {
        this.originalFiler.delete(filename);
    }

    @Override
    public long getContentLength(String filename) {
        return this.originalFiler.getContentLength(filename);
    }

    @Override
    public boolean exists(String filename) {
        return this.originalFiler.exists(filename);
    }

    @Override
    public String getAppendedFileExtension() {
        return ".gzip";
    }
}
