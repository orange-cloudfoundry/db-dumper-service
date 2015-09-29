package com.orange.clara.cloud.dbdump.s3;

import com.google.common.collect.Maps;
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

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.SortedMap;

/**
 * Copyright (C) 2015 Orange
 * <p/>
 * This software is distributed under the terms and conditions of the 'MIT'
 * license which can be found in the file 'LICENSE' in this package distribution
 * or at 'http://opensource.org/licenses/MIT'.
 * <p/>
 * Author: Arthur Halet
 * Date: 28/09/2015
 */
public class UploadS3Stream {

    public final static Integer CHUNK_SIZE = 5 * 1024 * 1024;

    @Autowired
    @Qualifier(value = "blobStoreContext")
    protected BlobStoreContext blobStoreContext;

    @Autowired
    @Qualifier(value = "bucketName")
    protected String bucketName;

    public String upload(InputStream content, Blob blob) throws IOException {
        content = new BufferedInputStream(content, CHUNK_SIZE);
        S3Client client = this.getS3Client();
        String key = blob.getMetadata().getName();
        ContentMetadata metadata = blob.getMetadata().getContentMetadata();
        ObjectMetadataBuilder builder = ObjectMetadataBuilder.create().key(key)
                .contentType(MediaType.OCTET_STREAM.toString())
                .contentDisposition(key)
                .contentEncoding(metadata.getContentEncoding())
                .contentLanguage(metadata.getContentLanguage())
                .userMetadata(blob.getMetadata().getUserMetadata());
        String uploadId = client.initiateMultipartUpload(bucketName, builder.build());
        Integer partNum = 1;
        Payload part = null;
        int bytesRead = 0;
        int i = 0;
        byte[] reader = new byte[1];
        byte[] chunk = new byte[CHUNK_SIZE];
        try {
            SortedMap<Integer, String> etags = Maps.newTreeMap();
            while (true) {
                bytesRead = content.read(reader);
                chunk[i] = reader[0];
                if (bytesRead == -1 && i < CHUNK_SIZE) {
                    chunk = Arrays.copyOf(chunk, i);
                }
                i++;
                if (i == CHUNK_SIZE || bytesRead == -1) {
                    part = new ByteArrayPayload(chunk);
                    prepareUploadPart(bucketName, key, uploadId, partNum, part, etags);
                    partNum++;
                    chunk = new byte[CHUNK_SIZE];
                    i = 0;
                }
                if (bytesRead == -1) {
                    break;
                }
            }
            return client.completeMultipartUpload(bucketName, key, uploadId, etags);
        } catch (RuntimeException ex) {
            client.abortMultipartUpload(bucketName, key, uploadId);
            throw ex;
        }
    }

    private void prepareUploadPart(String container, String key, String uploadId, int numPart, Payload chunkedPart, SortedMap<Integer, String> etags) {
        S3Client client = this.getS3Client();
        String eTag = null;
        try {
            eTag = client.uploadPart(container, key, numPart, uploadId, chunkedPart);
            etags.put(Integer.valueOf(numPart), eTag);
        } catch (KeyNotFoundException e) {
            // note that because of eventual consistency, the upload id may not be
            // present yet we may wish to add this condition to the retry handler

            // we may also choose to implement ListParts and wait for the uploadId
            // to become available there.
            eTag = client.uploadPart(container, key, numPart, uploadId, chunkedPart);
            etags.put(Integer.valueOf(numPart), eTag);
        }
    }

    private S3Client getS3Client() {
        return this.blobStoreContext.unwrapApi(S3Client.class);
    }
}
