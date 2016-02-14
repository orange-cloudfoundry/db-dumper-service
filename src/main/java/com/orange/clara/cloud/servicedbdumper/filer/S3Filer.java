package com.orange.clara.cloud.servicedbdumper.filer;

import com.google.common.io.ByteStreams;
import com.orange.clara.cloud.servicedbdumper.dbdumper.s3.UploadS3Stream;
import org.jclouds.blobstore.BlobStore;
import org.jclouds.blobstore.BlobStoreContext;
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
 * <p/>
 * This software is distributed under the terms and conditions of the 'MIT'
 * license which can be found in the file 'LICENSE' in this package distribution
 * or at 'http://opensource.org/licenses/MIT'.
 * <p/>
 * Author: Arthur Halet
 * Date: 24/11/2015
 */
public class S3Filer implements Filer {

    @Autowired
    @Qualifier(value = "uploadS3Stream")
    protected UploadS3Stream uploadS3Stream;

    @Autowired
    @Qualifier(value = "bucketName")
    protected String bucketName;

    @Autowired
    @Qualifier(value = "blobStoreContext")
    protected BlobStoreContext blobStoreContext;

    private Logger logger = LoggerFactory.getLogger(S3Filer.class);


    @Override
    public void store(InputStream inputStream, String filename) throws IOException {
        BlobStore blobStore = this.blobStoreContext.getBlobStore();
        Blob blob = blobStore.blobBuilder(filename).build();
        this.logger.info("Uploading dump file '" + filename + "'  on S3 storage.");
        this.uploadS3Stream.upload(inputStream, blob);
        inputStream.close();
    }

    @Override
    public void retrieve(OutputStream outputStream, String filename) throws IOException {
        BlobStore blobStore = this.blobStoreContext.getBlobStore();
        Blob blob = blobStore.getBlob(this.bucketName, filename);
        InputStream inputStream = blob.getPayload().openStream();
        ByteStreams.copy(inputStream, outputStream);
        outputStream.flush();
        inputStream.close();
        outputStream.close();
    }

    @Override
    public InputStream retrieveWithStream(String filename) throws IOException {
        BlobStore blobStore = this.blobStoreContext.getBlobStore();
        Blob blob = blobStore.getBlob(this.bucketName, filename);
        return blob.getPayload().openStream();
    }

    @Override
    public InputStream retrieveWithOriginalStream(String filename) throws IOException {
        BlobStore blobStore = this.blobStoreContext.getBlobStore();
        Blob blob = blobStore.getBlob(this.bucketName, filename);
        return blob.getPayload().openStream();
    }

    @Override
    public void delete(String filename) {
        BlobStore blobStore = this.blobStoreContext.getBlobStore();
        blobStore.removeBlob(this.bucketName, filename);
    }


    @Override
    public long getContentLength(String filename) {
        BlobStore blobStore = this.blobStoreContext.getBlobStore();
        Blob blob = blobStore.getBlob(this.bucketName, filename);
        return blob.getPayload().getContentMetadata().getContentLength();
    }

    @Override
    public String getAppendedFileExtension() {
        return "";
    }

    public void setUploadS3Stream(UploadS3Stream uploadS3Stream) {
        this.uploadS3Stream = uploadS3Stream;
    }

    public void setBucketName(String bucketName) {
        this.bucketName = bucketName;
    }

    public void setBlobStoreContext(BlobStoreContext blobStoreContext) {
        this.blobStoreContext = blobStoreContext;
    }
}
