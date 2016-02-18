package com.orange.clara.cloud.servicedbdumper.filer.s3uploader;

import com.google.common.collect.Maps;
import com.google.common.io.ByteStreams;
import com.google.common.net.MediaType;
import org.jclouds.blobstore.BlobStoreContext;
import org.jclouds.blobstore.KeyNotFoundException;
import org.jclouds.blobstore.domain.Blob;
import org.jclouds.io.ContentMetadata;
import org.jclouds.io.Payload;
import org.jclouds.io.payloads.ByteArrayPayload;
import org.jclouds.s3.S3Client;
import org.jclouds.s3.domain.ObjectMetadataBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.SortedMap;

/**
 * Copyright (C) 2015 Orange
 * <p/>
 * This software is distributed under the terms and conditions of the 'Apache-2.0'
 * license which can be found in the file 'LICENSE' in this package distribution
 * or at 'https://opensource.org/licenses/Apache-2.0'.
 * <p/>
 * Author: Arthur Halet
 * Date: 28/09/2015
 */
public class UploadS3StreamImpl implements UploadS3Stream {

    public final static Integer CHUNK_SIZE = 5 * 1024 * 1024; //set a chunk to 5MB

    @Autowired
    @Qualifier(value = "blobStoreContext")
    protected BlobStoreContext blobStoreContext;

    @Autowired
    @Qualifier(value = "bucketName")
    protected String bucketName;

    protected S3Client s3Client;

    @PostConstruct
    private void injectS3Client() {
        this.s3Client = this.blobStoreContext.unwrapApi(S3Client.class);
    }

    @Override
    public String upload(InputStream content, Blob blob) throws IOException {
        String key = blob.getMetadata().getName();
        ContentMetadata metadata = blob.getMetadata().getContentMetadata();
        ObjectMetadataBuilder builder = ObjectMetadataBuilder.create().key(key)
                .contentType(MediaType.OCTET_STREAM.toString())
                .contentDisposition(key)
                .contentEncoding(metadata.getContentEncoding())
                .contentLanguage(metadata.getContentLanguage())
                .userMetadata(blob.getMetadata().getUserMetadata());
        String uploadId = this.s3Client.initiateMultipartUpload(bucketName, builder.build());
        Integer partNum = 1;
        Payload part = null;
        int bytesRead = 0;
        boolean shouldContinue = true;
        try {
            SortedMap<Integer, String> etags = Maps.newTreeMap();
            while (shouldContinue) {
                byte[] chunk = new byte[CHUNK_SIZE];
                bytesRead = ByteStreams.read(content, chunk, 0, chunk.length);
                if (bytesRead != chunk.length) {
                    shouldContinue = false;
                    chunk = Arrays.copyOf(chunk, bytesRead);
                }
                part = new ByteArrayPayload(chunk);
                prepareUploadPart(bucketName, key, uploadId, partNum, part, etags);
                partNum++;
            }
            return this.s3Client.completeMultipartUpload(bucketName, key, uploadId, etags);
        } catch (RuntimeException ex) {
            this.s3Client.abortMultipartUpload(bucketName, key, uploadId);
            throw ex;
        }
    }

    private void prepareUploadPart(String container, String key, String uploadId, int numPart, Payload chunkedPart, SortedMap<Integer, String> etags) {
        String eTag = null;
        try {
            eTag = s3Client.uploadPart(container, key, numPart, uploadId, chunkedPart);
            etags.put(Integer.valueOf(numPart), eTag);
        } catch (KeyNotFoundException e) {
            // note that because of eventual consistency, the upload id may not be
            // present yet we may wish to add this condition to the retry handler

            // we may also choose to implement ListParts and wait for the uploadId
            // to become available there.
            eTag = s3Client.uploadPart(container, key, numPart, uploadId, chunkedPart);
            etags.put(Integer.valueOf(numPart), eTag);
        }
    }
}
