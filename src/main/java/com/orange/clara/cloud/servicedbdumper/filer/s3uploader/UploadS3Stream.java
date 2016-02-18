package com.orange.clara.cloud.servicedbdumper.filer.s3uploader;

import org.jclouds.blobstore.domain.Blob;

import java.io.IOException;
import java.io.InputStream;

/**
 * Copyright (C) 2015 Orange
 * <p/>
 * This software is distributed under the terms and conditions of the 'Apache-2.0'
 * license which can be found in the file 'LICENSE' in this package distribution
 * or at 'https://opensource.org/licenses/Apache-2.0'.
 * <p/>
 * Author: Arthur Halet
 * Date: 01/10/2015
 */
public interface UploadS3Stream {
    String upload(InputStream content, Blob blob) throws IOException;
}
