package com.orange.clara.cloud.servicedbdumper.filer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Copyright (C) 2015 Arthur Halet
 * <p/>
 * This software is distributed under the terms and conditions of the 'MIT'
 * license which can be found in the file 'LICENSE' in this package distribution
 * or at 'http://opensource.org/licenses/MIT'.
 * <p/>
 * Author: Arthur Halet
 * Date: 24/11/2015
 */
public interface Filer {
    void store(InputStream inputStream, String filename) throws IOException;

    void retrieve(OutputStream outputStream, String filename) throws IOException;

    InputStream retrieveWithStream(String filename) throws IOException;

    InputStream retrieveWithOriginalStream(String filename) throws IOException;

    void delete(String filename);

    long getContentLength(String filename);

    String getFileExtension();
}
