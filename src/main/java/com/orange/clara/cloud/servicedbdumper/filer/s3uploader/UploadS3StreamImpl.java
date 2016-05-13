package com.orange.clara.cloud.servicedbdumper.filer.s3uploader;

import com.google.common.collect.Maps;
import com.google.common.io.ByteStreams;
import com.google.common.net.MediaType;
import com.orange.spring.cloud.connector.s3.core.jcloudswrappers.SpringCloudBlobStoreContext;
import org.jclouds.blobstore.domain.Blob;
import org.jclouds.io.ContentMetadata;
import org.jclouds.io.Payload;
import org.jclouds.io.payloads.ByteArrayPayload;
import org.jclouds.s3.S3Client;
import org.jclouds.s3.domain.ObjectMetadataBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Map;
import java.util.SortedMap;

/**
 * Copyright (C) 2015 Orange
 * <p>
 * This software is distributed under the terms and conditions of the 'Apache-2.0'
 * license which can be found in the file 'LICENSE' in this package distribution
 * or at 'https://opensource.org/licenses/Apache-2.0'.
 * <p>
 * Author: Arthur Halet
 * Date: 28/09/2015
 */
public class UploadS3StreamImpl implements UploadS3Stream {
    public final static Integer DEFAULT_CHUNK_SIZE = 5 * 1024 * 1024;//set a chunk to 5MB
    protected Logger logger = LoggerFactory.getLogger(UploadS3StreamImpl.class);
    @Autowired
    @Qualifier(value = "blobStoreContext")
    protected SpringCloudBlobStoreContext blobStoreContext;
    protected S3Client s3Client;
    @Value("${s3.upload.retry:5}")
    protected int retry;
    private Integer chunkSize = DEFAULT_CHUNK_SIZE; //set a chunk to 5MB

    @PostConstruct
    private void injectS3Client() {
        this.s3Client = this.blobStoreContext.unwrapApi(S3Client.class);
    }

    @Override
    public String upload(InputStream content, Blob blob) throws IOException {
        String key = blob.getMetadata().getName();
        String bucketName = this.blobStoreContext.getBucketName();
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
                byte[] chunk = new byte[chunkSize];
                bytesRead = ByteStreams.read(content, chunk, 0, chunk.length);
                if (bytesRead != chunk.length) {
                    shouldContinue = false;
                    chunk = Arrays.copyOf(chunk, bytesRead);
                    if (chunk.length == 0) {
                        break;
                    }
                }
                part = new ByteArrayPayload(chunk);
                prepareUploadPart(bucketName, key, uploadId, partNum, part, etags);
                partNum++;
            }
            return this.completeMultipartUpload(bucketName, key, uploadId, etags);
        } catch (RuntimeException ex) {
            this.s3Client.abortMultipartUpload(bucketName, key, uploadId);
            throw ex;
        }
    }

    protected String completeMultipartUpload(String bucketName, String key, String uploadId, Map<Integer, String> parts) {
        int i = 1;
        while (true) {
            try {
                String complete = this.s3Client.completeMultipartUpload(bucketName, key, uploadId, parts);
                return complete;
            } catch (Exception e) {
                logger.warn("Retry {} to complete upload on S3.", i);
                if (i >= retry) {
                    throw e;
                }
            }
            i++;
        }
    }

    protected void prepareUploadPart(String container, String key, String uploadId, int numPart, Payload chunkedPart, SortedMap<Integer, String> etags) {
        String eTag = null;
        int i = 1;
        while (true) {
            try {
                eTag = s3Client.uploadPart(container, key, numPart, uploadId, chunkedPart);
                etags.put(Integer.valueOf(numPart), eTag);
                break;
            } catch (Exception e) {
                logger.warn("Retry {}/{} to upload on S3 for container {}.", i, retry, container);
                if (i >= retry) {
                    throw e;
                }
            }
            i++;
        }

    }

    public Integer getChunkSize() {
        return chunkSize;
    }

    public void setChunkSize(Integer chunkSize) {
        this.chunkSize = chunkSize;
    }
}
