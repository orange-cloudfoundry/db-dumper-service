package com.orange.clara.cloud.servicedbdumper.filer.s3uploader;

import com.google.common.io.Files;
import com.orange.clara.cloud.servicedbdumper.fake.s3client.FakeS3Client;
import com.orange.spring.cloud.connector.s3.core.jcloudswrappers.SpringCloudBlobStoreContext;
import org.jclouds.ContextBuilder;
import org.jclouds.blobstore.BlobStore;
import org.jclouds.blobstore.BlobStoreContext;
import org.jclouds.blobstore.domain.Blob;
import org.jclouds.filesystem.reference.FilesystemConstants;
import org.jclouds.io.Payload;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Spy;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Properties;
import java.util.SortedMap;

import static org.fest.assertions.Assertions.assertThat;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;

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
public class UploadS3StreamImplTest {
    protected final static String bucketName = "fakebucket";
    protected final static String fileName = "testFileS3Stream.txt";
    private final static String baseDirectory = System.getProperty("java.io.tmpdir");
    private final static String content = "this is a test";
    @Spy
    UploadS3StreamImpl uploadS3Stream;

    private Blob blob;
    private SpringCloudBlobStoreContext springCloudBlobStoreContext;

    @Before
    public void init() {
        initMocks(this);
        uploadS3Stream.s3Client = new FakeS3Client(baseDirectory);
        Properties properties = new Properties();
        properties.setProperty(FilesystemConstants.PROPERTY_BASEDIR, baseDirectory);
        BlobStoreContext blobStoreContext = ContextBuilder.newBuilder("filesystem")
                .overrides(properties)
                .buildView(BlobStoreContext.class);
        this.springCloudBlobStoreContext = new SpringCloudBlobStoreContext(blobStoreContext, bucketName);
        uploadS3Stream.blobStoreContext = this.springCloudBlobStoreContext;
        blobStoreContext.getBlobStore().createContainerInLocation(null, bucketName);
        this.uploadS3Stream.setChunkSize(UploadS3StreamImpl.DEFAULT_CHUNK_SIZE);
        BlobStore blobStore = springCloudBlobStoreContext.getBlobStore();
        blob = blobStore.blobBuilder(fileName).build();
    }

    @Test
    public void when_upload_a_blob_then_file_should_be_created() throws IOException {
        this.uploadS3Stream.upload(new ByteArrayInputStream(content.getBytes()), blob);
        this.assertFileUploaded();
    }

    @Test
    public void ensure_blob_is_sent_in_multiple_part() throws IOException {
        this.uploadS3Stream.setChunkSize(1);
        this.uploadS3Stream.upload(new ByteArrayInputStream(content.getBytes()), blob);
        this.assertFileUploaded();
        verify(uploadS3Stream, times(content.getBytes().length)).prepareUploadPart(
                anyString(), anyString(), anyString(),
                anyInt(),
                (Payload) notNull(),
                (SortedMap<Integer, String>) notNull()
        );
    }

    public void assertFileUploaded() throws IOException {
        File expectedFile = new File(baseDirectory + "/" + bucketName + "/" + fileName);
        assertThat(expectedFile.isFile()).isTrue();
        assertThat(Files.toString(expectedFile, Charset.defaultCharset())).isEqualTo(content);
    }

    @After
    public void clean() {
        File file = new File(baseDirectory + "/" + bucketName + "/" + fileName);
        if (!file.isFile()) {
            return;
        }
        file.delete();
        if (file.getParentFile().isDirectory()) {
            file.getParentFile().delete();
        }
    }
}