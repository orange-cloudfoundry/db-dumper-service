package com.orange.clara.cloud.riak;

import com.basho.riakcs.client.api.RiakCSClient;
import com.basho.riakcs.client.api.RiakCSException;
import com.basho.riakcs.client.impl.RiakCSClientImpl;
import org.json.JSONObject;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;

/**
 * Copyright (C) 2015 Arthur Halet
 * <p/>
 * This software is distributed under the terms and conditions of the 'MIT'
 * license which can be found in the file 'LICENSE' in this package distribution
 * or at 'http://opensource.org/licenses/MIT'.
 * <p/>
 * Author: Arthur Halet
 * Date: 03/06/2015
 */
public class RiakcsClientCloudFoundry extends RiakCSClient {
    private String bucketName;


    public RiakcsClientCloudFoundry(String csAccessKey, String csSecretKey, String csEndpoint, boolean useSSL, String bucketName) {
        super(csAccessKey, csSecretKey, csEndpoint, useSSL);
        this.bucketName = bucketName;
    }

    public RiakcsClientCloudFoundry(String csAccessKey, String csSecretKey, String bucketName) {
        super(csAccessKey, csSecretKey);
        this.bucketName = bucketName;
    }

    public static void copyBucketBetweenSystems(RiakcsClientCloudFoundry fromSystem, RiakcsClientCloudFoundry toSystem) throws RiakCSException {
        if (!fromSystem.isBucketAccessible()) {
            throw new RiakCSException("Source Bucket doesn\'t exist");
        } else if (toSystem.isBucketAccessible()) {
            throw new RiakCSException("Bucket already exists, choose different bucket name");
        } else {
            toSystem.createBucket();
            RiakCSClientImpl.copyBucketBetweenSystems(fromSystem, fromSystem.getBucketName(), toSystem, toSystem.getBucketName());
        }
    }

    public String getBucketName() {
        return bucketName;
    }

    public void setBucketName(String bucketName) {
        this.bucketName = bucketName;
    }

    public boolean isBucketAccessible() throws RiakCSException {
        return this.isBucketAccessible(this.bucketName);
    }

    public JSONObject getACLForBucket() throws RiakCSException {
        return this.getACLForBucket(bucketName);
    }

    public void deleteBucket() throws RiakCSException {
        this.deleteBucket(bucketName);
    }

    public void createObject(String objectKey, InputStream dataInputStream, Map<String, String> headers, Map<String, String> metadata) throws RiakCSException {
        this.createObject(bucketName, objectKey, dataInputStream, headers, metadata);
    }

    public JSONObject listObjects() throws RiakCSException {
        return this.listObjects(bucketName);
    }

    public JSONObject listObjectNames() throws RiakCSException {
        return this.listObjects(bucketName);
    }

    public JSONObject getObject(String objectKey) throws RiakCSException {
        return this.getObject(bucketName, objectKey);
    }

    public JSONObject getObject(String objectKey, OutputStream dataOutputStream) throws RiakCSException {
        return this.getObject(bucketName, objectKey, dataOutputStream);
    }

    public JSONObject getObjectInfo(String objectKey) throws RiakCSException {
        return this.getObjectInfo(bucketName, objectKey);
    }

    public JSONObject getACLForObject(String objectKey) throws RiakCSException {
        return this.getACLForObject(bucketName, objectKey);
    }

    public void deleteObject(String objectKey) throws RiakCSException {
        this.deleteObject(bucketName, objectKey);
    }

    public void setCannedACLForBucket(String cannedACL) throws RiakCSException {
        this.setCannedACLForBucket(bucketName, cannedACL);
    }

    public void setCannedACLForObject(String objectKey, String cannedACL) throws RiakCSException {
        this.setCannedACLForObject(bucketName, objectKey, cannedACL);
    }

    public void addAdditionalACLToBucket(String emailAddress, RiakCSClient.Permission permission) throws RiakCSException {
        this.addAdditionalACLToBucket(bucketName, emailAddress, permission);
    }

    public void addAdditionalACLToObject(String objectKey, String emailAddress, RiakCSClient.Permission permission) throws RiakCSException {
        this.addAdditionalACLToObject(bucketName, objectKey, emailAddress, permission);
    }

    public void removeBucketAndContent() throws RiakCSException {
        this.removeBucketAndContent(bucketName);
    }

    public void uploadContentOfDirectory(File fromDirectory) throws RiakCSException {
        this.uploadContentOfDirectory(fromDirectory, this.bucketName);
    }

    public void createBucket() throws RiakCSException {
        this.createBucket(bucketName);
    }
}
