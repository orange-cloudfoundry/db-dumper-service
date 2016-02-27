package com.orange.clara.cloud.servicedbdumper.dbdumper.fake;

import com.google.common.base.Charsets;
import com.google.common.io.ByteStreams;
import com.google.common.io.CharStreams;
import com.orange.clara.cloud.servicedbdumper.filer.Filer;
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
 * Date: 26/02/2016
 */
public class EchoFiler implements Filer {

    private String text;
    private Logger logger = LoggerFactory.getLogger(EchoFiler.class);

    public EchoFiler(String text) {
        this.text = text;
    }

    @Override
    public void store(InputStream inputStream, String filename) throws IOException {
        String echoed = CharStreams.toString(new InputStreamReader(inputStream, Charsets.UTF_8));
        logger.info("Echo filer stored: " + echoed);
        inputStream.close();
    }

    @Override
    public void retrieve(OutputStream outputStream, String filename) throws IOException {
        InputStream is = new ByteArrayInputStream(this.text.getBytes());
        ByteStreams.copy(is, outputStream);
        try {
            outputStream.flush();
        } catch (IOException e) {
        }
        is.close();
        try {
            outputStream.close();
        } catch (IOException e) {
        }
    }

    @Override
    public InputStream retrieveWithStream(String filename) throws IOException {
        return new ByteArrayInputStream(this.text.getBytes());
    }

    @Override
    public InputStream retrieveWithOriginalStream(String filename) throws IOException {
        return new ByteArrayInputStream(this.text.getBytes());
    }

    @Override
    public void delete(String filename) {

    }

    @Override
    public long getContentLength(String filename) {
        return 1;
    }

    @Override
    public String getAppendedFileExtension() {
        return "";
    }
}
