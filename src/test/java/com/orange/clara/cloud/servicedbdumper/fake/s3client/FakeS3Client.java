package com.orange.clara.cloud.servicedbdumper.fake.s3client;

import com.google.common.io.Files;
import org.jclouds.http.options.GetOptions;
import org.jclouds.io.Payload;
import org.jclouds.io.payloads.ByteArrayPayload;
import org.jclouds.javax.annotation.Nullable;
import org.jclouds.rest.annotations.BinderParam;
import org.jclouds.rest.annotations.EndpointParam;
import org.jclouds.rest.annotations.ParamParser;
import org.jclouds.rest.annotations.ParamValidators;
import org.jclouds.s3.Bucket;
import org.jclouds.s3.S3Client;
import org.jclouds.s3.binders.*;
import org.jclouds.s3.domain.*;
import org.jclouds.s3.functions.*;
import org.jclouds.s3.options.CopyObjectOptions;
import org.jclouds.s3.options.ListBucketOptions;
import org.jclouds.s3.options.PutBucketOptions;
import org.jclouds.s3.options.PutObjectOptions;
import org.jclouds.s3.predicates.validators.BucketNameValidator;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

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
public class FakeS3Client implements S3Client {

    private String baseFolder;

    public FakeS3Client(String baseFolder) {
        this.baseFolder = baseFolder;
    }

    @Override
    public S3Object newS3Object() {
        return null;
    }

    @Override
    public S3Object getObject(@Bucket @EndpointParam(parser = AssignCorrectHostnameForBucket.class) @BinderParam(BindAsHostPrefixIfConfigured.class) @ParamValidators({BucketNameValidator.class}) String bucketName, String key, GetOptions... options) {
        return null;
    }

    @Override
    public ObjectMetadata headObject(@Bucket @EndpointParam(parser = AssignCorrectHostnameForBucket.class) @BinderParam(BindAsHostPrefixIfConfigured.class) @ParamValidators({BucketNameValidator.class}) String bucketName, String key) {
        return null;
    }

    @Override
    public boolean objectExists(@Bucket @EndpointParam(parser = AssignCorrectHostnameForBucket.class) @BinderParam(BindAsHostPrefixIfConfigured.class) @ParamValidators({BucketNameValidator.class}) String bucketName, String key) {
        return false;
    }

    @Override
    public void deleteObject(@Bucket @EndpointParam(parser = AssignCorrectHostnameForBucket.class) @BinderParam(BindAsHostPrefixIfConfigured.class) @ParamValidators({BucketNameValidator.class}) String bucketName, String key) {

    }

    @Override
    public DeleteResult deleteObjects(@Bucket @EndpointParam(parser = AssignCorrectHostnameForBucket.class) @BinderParam(BindAsHostPrefixIfConfigured.class) @ParamValidators({BucketNameValidator.class}) String bucketName, @BinderParam(BindIterableAsPayloadToDeleteRequest.class) Iterable<String> keys) {
        return null;
    }

    @Override
    public String putObject(@Bucket @EndpointParam(parser = AssignCorrectHostnameForBucket.class) @BinderParam(BindAsHostPrefixIfConfigured.class) @ParamValidators({BucketNameValidator.class}) String bucketName, @ParamParser(ObjectKey.class) @BinderParam(BindS3ObjectMetadataToRequest.class) S3Object object, PutObjectOptions... options) {
        return null;
    }

    @Override
    public boolean putBucketInRegion(@BinderParam(BindRegionToXmlPayload.class) @Nullable String region, @Bucket @BinderParam(BindAsHostPrefixIfConfigured.class) @ParamValidators({BucketNameValidator.class}) String bucketName, PutBucketOptions... options) {
        return false;
    }

    @Override
    public boolean deleteBucketIfEmpty(@Bucket @EndpointParam(parser = DefaultEndpointThenInvalidateRegion.class) @BinderParam(BindAsHostPrefixIfConfigured.class) @ParamValidators({BucketNameValidator.class}) String bucketName) {
        return false;
    }

    @Override
    public boolean bucketExists(@Bucket @EndpointParam(parser = AssignCorrectHostnameForBucket.class) @BinderParam(BindAsHostPrefixIfConfigured.class) @ParamValidators({BucketNameValidator.class}) String bucketName) {
        return false;
    }

    @Override
    public ListBucketResponse listBucket(@Bucket @EndpointParam(parser = AssignCorrectHostnameForBucket.class) @BinderParam(BindAsHostPrefixIfConfigured.class) @ParamValidators({BucketNameValidator.class}) String bucketName, ListBucketOptions... options) {
        return null;
    }

    @Override
    public Set<BucketMetadata> listOwnedBuckets() {
        return null;
    }

    @Override
    public ObjectMetadata copyObject(String sourceBucket, String sourceObject, @Bucket @EndpointParam(parser = AssignCorrectHostnameForBucket.class) @BinderParam(BindAsHostPrefixIfConfigured.class) @ParamValidators({BucketNameValidator.class}) String destinationBucket, String destinationObject, CopyObjectOptions... options) {
        return null;
    }

    @Override
    public AccessControlList getBucketACL(@Bucket @EndpointParam(parser = AssignCorrectHostnameForBucket.class) @BinderParam(BindAsHostPrefixIfConfigured.class) @ParamValidators({BucketNameValidator.class}) String bucketName) {
        return null;
    }

    @Override
    public boolean putBucketACL(@Bucket @EndpointParam(parser = AssignCorrectHostnameForBucket.class) @BinderParam(BindAsHostPrefixIfConfigured.class) @ParamValidators({BucketNameValidator.class}) String bucketName, @BinderParam(BindACLToXMLPayload.class) AccessControlList acl) {
        return false;
    }

    @Override
    public AccessControlList getObjectACL(@Bucket @EndpointParam(parser = AssignCorrectHostnameForBucket.class) @BinderParam(BindAsHostPrefixIfConfigured.class) @ParamValidators({BucketNameValidator.class}) String bucketName, String key) {
        return null;
    }

    @Override
    public boolean putObjectACL(@Bucket @EndpointParam(parser = AssignCorrectHostnameForBucket.class) @BinderParam(BindAsHostPrefixIfConfigured.class) @ParamValidators({BucketNameValidator.class}) String bucketName, String key, @BinderParam(BindACLToXMLPayload.class) AccessControlList acl) {
        return false;
    }

    @Override
    public String getBucketLocation(@Bucket @BinderParam(BindAsHostPrefixIfConfigured.class) @ParamValidators({BucketNameValidator.class}) String bucketName) {
        return null;
    }

    @Override
    public Payer getBucketPayer(@Bucket @EndpointParam(parser = AssignCorrectHostnameForBucket.class) @BinderParam(BindAsHostPrefixIfConfigured.class) @ParamValidators({BucketNameValidator.class}) String bucketName) {
        return null;
    }

    @Override
    public void setBucketPayer(@Bucket @EndpointParam(parser = AssignCorrectHostnameForBucket.class) @BinderParam(BindAsHostPrefixIfConfigured.class) @ParamValidators({BucketNameValidator.class}) String bucketName, @BinderParam(BindPayerToXmlPayload.class) Payer payer) {

    }

    @Override
    public BucketLogging getBucketLogging(@Bucket @EndpointParam(parser = AssignCorrectHostnameForBucket.class) @BinderParam(BindAsHostPrefixIfConfigured.class) @ParamValidators({BucketNameValidator.class}) String bucketName) {
        return null;
    }

    @Override
    public void enableBucketLogging(@Bucket @EndpointParam(parser = AssignCorrectHostnameForBucket.class) @BinderParam(BindAsHostPrefixIfConfigured.class) @ParamValidators({BucketNameValidator.class}) String bucketName, @BinderParam(BindBucketLoggingToXmlPayload.class) BucketLogging logging) {

    }

    @Override
    public void disableBucketLogging(@Bucket @EndpointParam(parser = AssignCorrectHostnameForBucket.class) @BinderParam(BindNoBucketLoggingToXmlPayload.class) @ParamValidators({BucketNameValidator.class}) String bucketName) {

    }

    @Override
    public String initiateMultipartUpload(@Bucket @EndpointParam(parser = AssignCorrectHostnameForBucket.class) @BinderParam(BindAsHostPrefixIfConfigured.class) @ParamValidators({BucketNameValidator.class}) String bucketName, @ParamParser(ObjectMetadataKey.class) @BinderParam(BindObjectMetadataToRequest.class) ObjectMetadata objectMetadata, PutObjectOptions... options) {
        return UUID.randomUUID().toString();
    }

    @Override
    public void abortMultipartUpload(@Bucket @EndpointParam(parser = AssignCorrectHostnameForBucket.class) @BinderParam(BindAsHostPrefixIfConfigured.class) @ParamValidators({BucketNameValidator.class}) String bucketName, String key, String uploadId) {

    }

    @Override
    public String uploadPart(@Bucket @EndpointParam(parser = AssignCorrectHostnameForBucket.class) @BinderParam(BindAsHostPrefixIfConfigured.class) @ParamValidators({BucketNameValidator.class}) String bucketName, String key, int partNumber, String uploadId, Payload part) {
        String folderPath = this.baseFolder + "/" + bucketName;
        File folder = new File(folderPath);
        folder.mkdirs();
        File fileToFeed = new File(folderPath + "/" + key);
        if (!fileToFeed.isFile()) {
            try {
                fileToFeed.createNewFile();
            } catch (IOException e) {
                throw new RuntimeException(e.getMessage(), e);
            }
        }
        ByteArrayPayload partOriginal = (ByteArrayPayload) part;
        byte[] partOriginalBytes = partOriginal.getRawContent();
        try {
            byte[] currentContent = Files.toByteArray(fileToFeed);

            byte[] toWrite = new byte[currentContent.length + partOriginalBytes.length];
            System.arraycopy(currentContent, 0, toWrite, 0, currentContent.length);
            System.arraycopy(partOriginalBytes, 0, toWrite, currentContent.length, partOriginalBytes.length);
            Files.write(toWrite, fileToFeed);
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
        return "etag-" + partNumber;
    }

    @Override
    public String completeMultipartUpload(@Bucket @EndpointParam(parser = AssignCorrectHostnameForBucket.class) @BinderParam(BindAsHostPrefixIfConfigured.class) @ParamValidators({BucketNameValidator.class}) String bucketName, String key, String uploadId, @BinderParam(BindPartIdsAndETagsToRequest.class) Map<Integer, String> parts) {
        return uploadId;
    }

    @Override
    public void close() throws IOException {

    }
}
