package com.orange.clara.cloud.servicedbdumper.filer;

import com.orange.clara.cloud.servicedbdumper.filer.chunk.ChunkStream;
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
 * Date: 21/09/2016
 */
public class ChunkFiler implements Filer {
    protected Filer originalFiler;
    protected ChunkStream chunkStream;
    private Logger logger = LoggerFactory.getLogger(ChunkFiler.class);

    public ChunkFiler(Filer originalFiler, ChunkStream chunkStream) {
        this.originalFiler = originalFiler;
        this.chunkStream = chunkStream;
    }

    public Filer getOriginalFiler() {
        return originalFiler;
    }

    public void setOriginalFiler(Filer originalFiler) {
        this.originalFiler = originalFiler;
    }

    public ChunkStream getChunkStream() {
        return chunkStream;
    }

    public void setChunkStream(ChunkStream chunkStream) {
        this.chunkStream = chunkStream;
    }

    @Override
    public void store(InputStream inputStream, String filename) throws IOException {
        originalFiler.store(inputStream, filename);
    }

    @Override
    public void retrieve(OutputStream outputStream, String filename) throws IOException {
        InputStream inputStream = this.originalFiler.retrieveWithStream(filename);
        this.chunkStream.chunkIt(outputStream, inputStream);
        logger.debug("Chunking file for sending ...");
    }

    @Override
    public InputStream retrieveWithStream(String filename) throws IOException {
        PipedOutputStream outputPipe = new PipedOutputStream();
        PipedInputStream inputPipe = new PipedInputStream(outputPipe);

        InputStream inputStream = this.originalFiler.retrieveWithStream(filename);
        this.chunkStream.chunkIt(outputPipe, inputStream);
        logger.debug("Chunking file for sending ...");
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
        return this.originalFiler.getAppendedFileExtension();
    }
}
