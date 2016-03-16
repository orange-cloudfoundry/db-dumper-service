package com.orange.clara.cloud.servicedbdumper.dbdumper.fake.s3uploader;

import com.orange.clara.cloud.servicedbdumper.filer.s3uploader.UploadS3Stream;
import org.jclouds.blobstore.BlobStore;
import org.jclouds.blobstore.BlobStoreContext;
import org.jclouds.blobstore.domain.Blob;

import java.io.IOException;
import java.io.InputStream;

/**
 * Copyright (C) 2016 Orange
 * <p>
 * This software is distributed under the terms and conditions of the 'Apache-2.0'
 * license which can be found in the file 'LICENSE' in this package distribution
 * or at 'https://opensource.org/licenses/Apache-2.0'.
 * <p>
 * Author: Arthur Halet
 * Date: 16/03/2016
 */
public class UploadS3StreamFake implements UploadS3Stream {
    private final static String containerName = "testuploads3";
    private BlobStoreContext blobStoreContext;

    public UploadS3StreamFake(BlobStoreContext blobStoreContext) {
        this.blobStoreContext = blobStoreContext;
    }

    public static String getContainerName() {
        return containerName;
    }

    @Override
    public String upload(InputStream content, Blob blob) throws IOException {
        BlobStore blobStore = this.blobStoreContext.getBlobStore();
        blob.setPayload(content);
        blobStore.putBlob(containerName, blob);
        return "etag";
    }

    public BlobStoreContext getBlobStoreContext() {
        return blobStoreContext;
    }

    public void setBlobStoreContext(BlobStoreContext context) {
        this.blobStoreContext = context;
    }
}
