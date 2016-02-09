package com.orange.clara.cloud.servicedbdumper.filer.compression;

import com.google.common.io.ByteStreams;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedOutputStream;
import java.util.concurrent.Future;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

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
@Service
public class GzipCompressing {
    @Async
    public Future<Boolean> gunziptIt(OutputStream outputStream, InputStream inputStream) throws IOException {
        GZIPInputStream gzis = new GZIPInputStream(inputStream);
        ByteStreams.copy(gzis, outputStream);
        outputStream.flush();
        outputStream.close();
        return new AsyncResult<Boolean>(true);
    }

    @Async
    public Future<Boolean> gziptIt(InputStream inputStream, PipedOutputStream outputPipe) throws IOException {
        GZIPOutputStream gout = new GZIPOutputStream(outputPipe);
        ByteStreams.copy(inputStream, gout);
        gout.flush();
        gout.close();
        outputPipe.flush();
        outputPipe.close();
        return new AsyncResult<Boolean>(true);
    }
}
