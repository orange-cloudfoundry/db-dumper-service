package com.orange.clara.cloud.dbdump.s3;

import org.jclouds.blobstore.domain.Blob;

import java.io.IOException;
import java.io.InputStream;

/**
 * Copyright (C) 2015 Arthur Halet
 * <p/>
 * This software is distributed under the terms and conditions of the 'MIT'
 * license which can be found in the file 'LICENSE' in this package distribution
 * or at 'http://opensource.org/licenses/MIT'.
 * <p/>
 * Author: Arthur Halet
 * Date: 01/10/2015
 */
public interface UploadS3Stream {
    String upload(InputStream content, Blob blob) throws IOException;
}
