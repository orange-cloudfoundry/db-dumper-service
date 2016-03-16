package com.orange.clara.cloud.servicedbdumper.filer;

import com.orange.clara.cloud.servicedbdumper.filer.compression.GzipCompressing;
import org.junit.Before;

/**
 * Copyright (C) 2016 Arthur Halet
 * <p>
 * This software is distributed under the terms and conditions of the 'MIT'
 * license which can be found in the file 'LICENSE' in this package distribution
 * or at 'http://opensource.org/licenses/MIT'.
 * <p>
 * Author: Arthur Halet
 * Date: 16/03/2016
 */
public class GzipS3FilerTest extends S3FilerTest {

    @Before
    public void init() {
        GzipS3Filer s3Filer = new GzipS3Filer();
        s3Filer.blobStore = this.springCloudBlobStore;
        s3Filer.uploadS3Stream = this.uploadS3StreamFake;
        s3Filer.gzipCompressing = new GzipCompressing();
        s3Filer.loadFilerDependencies();
        this.filer = s3Filer;
        this.toCompressedRawContent();
    }
}