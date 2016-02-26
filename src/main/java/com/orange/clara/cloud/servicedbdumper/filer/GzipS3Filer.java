package com.orange.clara.cloud.servicedbdumper.filer;

import com.orange.clara.cloud.servicedbdumper.filer.s3uploader.UploadS3Stream;
import com.orange.spring.cloud.connector.s3.core.jcloudswrappers.SpringCloudBlobStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import javax.annotation.PostConstruct;

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
public class GzipS3Filer extends AbstractGzipGenericFiler implements Filer {
    @Autowired
    @Qualifier(value = "uploadS3Stream")
    protected UploadS3Stream uploadS3Stream;


    @Autowired
    @Qualifier(value = "blobStore")
    protected SpringCloudBlobStore blobStore;

    public GzipS3Filer() {
        super(new S3Filer());
    }

    @PostConstruct
    public void loadFilerDependances() {
        S3Filer s3Filer = (S3Filer) this.originalFiler;
        s3Filer.setBlobStore(this.blobStore);
        s3Filer.setUploadS3Stream(this.uploadS3Stream);
    }


}
