package com.orange.clara.cloud.servicedbdumper.filer.compression;

import com.google.common.io.ByteStreams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.Future;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * Copyright (C) 2016 Orange
 * <p>
 * This software is distributed under the terms and conditions of the 'Apache-2.0'
 * license which can be found in the file 'LICENSE' in this package distribution
 * or at 'https://opensource.org/licenses/Apache-2.0'.
 * <p>
 * Author: Arthur Halet
 * Date: 09/02/2016
 */
@Service
public class GzipCompressing {
    private Logger logger = LoggerFactory.getLogger(GzipCompressing.class);

    @Async
    public Future<Boolean> gunziptIt(OutputStream outputStream, InputStream inputStream) throws IOException {
        logger.debug("Start uncompressing...");
        GZIPInputStream gzis = new GZIPInputStream(inputStream);
        ByteStreams.copy(gzis, outputStream);
        outputStream.flush();
        outputStream.close();
        logger.debug("Finish uncompressing");
        return new AsyncResult<Boolean>(true);
    }

    @Async
    public Future<Boolean> gziptIt(InputStream inputStream, OutputStream outputStream) throws IOException {
        logger.debug("Start compressing...");
        GZIPOutputStream gout = new GZIPOutputStream(outputStream);
        ByteStreams.copy(inputStream, gout);
        gout.flush();
        gout.close();
        outputStream.flush();
        outputStream.close();
        logger.debug("Finish compressing");
        return new AsyncResult<Boolean>(true);
    }
}
