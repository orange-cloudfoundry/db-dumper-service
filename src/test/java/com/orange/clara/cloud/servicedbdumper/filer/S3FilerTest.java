package com.orange.clara.cloud.servicedbdumper.filer;

import com.orange.clara.cloud.servicedbdumper.fake.s3uploader.UploadS3StreamFake;
import com.orange.spring.cloud.connector.s3.core.jcloudswrappers.SpringCloudBlobStore;
import com.orange.spring.cloud.connector.s3.core.jcloudswrappers.SpringCloudBlobStoreContext;
import org.jclouds.ContextBuilder;
import org.jclouds.blobstore.BlobStore;
import org.jclouds.blobstore.BlobStoreContext;
import org.jclouds.filesystem.reference.FilesystemConstants;
import org.junit.Before;

import java.util.Properties;

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
public class S3FilerTest extends AbstractFilerTest {

    protected final static String baseDirectory = System.getProperty("java.io.tmpdir");
    protected final static String bucketName = UploadS3StreamFake.getContainerName();
    protected final static String fileName = "testFile.txt";
    protected SpringCloudBlobStoreContext springCloudBlobStoreContext;
    protected SpringCloudBlobStore springCloudBlobStore;
    protected UploadS3StreamFake uploadS3StreamFake;

    public S3FilerTest() {
        super(baseDirectory + "/" + bucketName, fileName, null);
        Properties properties = new Properties();
        properties.setProperty(FilesystemConstants.PROPERTY_BASEDIR, baseDirectory);
        BlobStoreContext blobStoreContext = ContextBuilder.newBuilder("filesystem")
                .overrides(properties)
                .buildView(BlobStoreContext.class);
        springCloudBlobStoreContext = new SpringCloudBlobStoreContext(blobStoreContext, bucketName);

        this.createContainer(blobStoreContext.getBlobStore());
        this.springCloudBlobStore = new SpringCloudBlobStore(springCloudBlobStoreContext.getBlobStore(), bucketName, springCloudBlobStoreContext);
        this.uploadS3StreamFake = new UploadS3StreamFake(blobStoreContext);
    }

    @Before
    public void init() {
        S3Filer s3Filer = new S3Filer();
        s3Filer.blobStore = this.springCloudBlobStore;
        s3Filer.uploadS3Stream = this.uploadS3StreamFake;
        this.filer = s3Filer;
    }

    public void createContainer(BlobStore blobStore) {
        blobStore.createContainerInLocation(null, bucketName);
    }
}