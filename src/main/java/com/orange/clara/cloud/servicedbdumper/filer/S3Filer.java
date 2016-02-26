package com.orange.clara.cloud.servicedbdumper.filer;

import com.google.common.io.ByteStreams;
import com.orange.clara.cloud.servicedbdumper.filer.s3uploader.UploadS3Stream;
import com.orange.spring.cloud.connector.s3.core.jcloudswrappers.SpringCloudBlobStore;
import org.jclouds.blobstore.domain.Blob;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Copyright (C) 2015 Orange
 * <p>
 * This software is distributed under the terms and conditions of the 'Apache-2.0'
 * license which can be found in the file 'LICENSE' in this package distribution
 * or at 'https://opensource.org/licenses/Apache-2.0'.
 * <p>
 * Author: Arthur Halet
 * Date: 24/11/2015
 */
public class S3Filer implements Filer {

    @Autowired
    @Qualifier(value = "uploadS3Stream")
    protected UploadS3Stream uploadS3Stream;


    @Autowired
    @Qualifier(value = "blobStore")
    protected SpringCloudBlobStore blobStore;

    private Logger logger = LoggerFactory.getLogger(S3Filer.class);


    @Override
    public void store(InputStream inputStream, String filename) throws IOException {
        Blob blob = blobStore.blobBuilder(filename).build();
        this.logger.info("Uploading dump file '" + filename + "'  on S3 storage.");
        this.uploadS3Stream.upload(inputStream, blob);
        inputStream.close();
    }

    @Override
    public void retrieve(OutputStream outputStream, String filename) throws IOException {
        Blob blob = blobStore.getBlob(filename);
        InputStream inputStream = blob.getPayload().openStream();
        ByteStreams.copy(inputStream, outputStream);
        outputStream.flush();
        inputStream.close();
        outputStream.close();
    }

    @Override
    public InputStream retrieveWithStream(String filename) throws IOException {
        Blob blob = blobStore.getBlob(filename);
        return blob.getPayload().openStream();
    }

    @Override
    public InputStream retrieveWithOriginalStream(String filename) throws IOException {
        Blob blob = blobStore.getBlob(filename);
        return blob.getPayload().openStream();
    }

    @Override
    public void delete(String filename) {
        blobStore.removeBlob(filename);
    }


    @Override
    public long getContentLength(String filename) {
        Blob blob = blobStore.getBlob(filename);
        return blob.getPayload().getContentMetadata().getContentLength();
    }

    @Override
    public String getAppendedFileExtension() {
        return "";
    }

    public void setUploadS3Stream(UploadS3Stream uploadS3Stream) {
        this.uploadS3Stream = uploadS3Stream;
    }

    public SpringCloudBlobStore getBlobStore() {
        return blobStore;
    }

    public void setBlobStore(SpringCloudBlobStore blobStore) {
        this.blobStore = blobStore;
    }
}
