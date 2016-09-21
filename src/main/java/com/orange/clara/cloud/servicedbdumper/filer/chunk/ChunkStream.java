package com.orange.clara.cloud.servicedbdumper.filer.chunk;

import com.google.common.io.ByteStreams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.concurrent.Future;

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
@Service
public class ChunkStream {
    public final static Integer DEFAULT_CHUNK_SIZE = 5 * 1024 * 1024;
    private Logger logger = LoggerFactory.getLogger(ChunkStream.class);
    private Integer chunkSize = DEFAULT_CHUNK_SIZE;

    @Async
    public Future<Boolean> chunkIt(OutputStream outputStream, InputStream inputStream) throws IOException {
        logger.debug("Start chunking...");
        int bytesRead = 0;
        byte[] chunk = null;
        boolean shouldContinue = true;
        while (shouldContinue) {
            chunk = new byte[chunkSize];
            bytesRead = this.readInChunk(inputStream, chunk);
            if (bytesRead != chunk.length) {
                shouldContinue = false;
                chunk = Arrays.copyOf(chunk, bytesRead);
                if (chunk.length == 0) {
                    chunk = null;
                    break;
                }
            }
            outputStream.write(chunk);
            chunk = null;
        }
        outputStream.flush();
        outputStream.close();
        inputStream.close();
        logger.debug("Finish chunking");
        return new AsyncResult<Boolean>(true);
    }

    protected int readInChunk(InputStream inputStream, byte[] chunk) throws IOException {
        return ByteStreams.read(inputStream, chunk, 0, chunk.length);
    }

    public Integer getChunkSize() {
        return chunkSize;
    }

    public void setChunkSize(Integer chunkSize) {
        this.chunkSize = chunkSize;
    }
}
