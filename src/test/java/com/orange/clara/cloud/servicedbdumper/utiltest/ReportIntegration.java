package com.orange.clara.cloud.servicedbdumper.utiltest;

import java.util.UUID;

/**
 * Copyright (C) 2016 Orange
 * <p>
 * This software is distributed under the terms and conditions of the 'Apache-2.0'
 * license which can be found in the file 'LICENSE' in this package distribution
 * or at 'https://opensource.org/licenses/Apache-2.0'.
 * <p>
 * Author: Arthur Halet
 * Date: 20/05/2016
 */
public class ReportIntegration {
    private String id;
    private String name;
    private long populateFakeDataTime = 0L;
    private long populateToDatabaseTime = 0L;
    private long dumpDatabaseSourceTime = 0L;
    private long restoreDatabaseSourceToTargetTime = 0L;
    private long dumpDatabaseTargetTime = 0L;
    private long fakeDataFileSize = 0L;
    private long diffTime = 0L;
    private boolean skipped = false;
    private String skippedReason = "";

    public ReportIntegration(String name) {
        this.setName(name);
    }

    public long getPopulateFakeDataTime() {
        return populateFakeDataTime;
    }

    public void setPopulateFakeDataTime(long populateFakeDataTime) {
        this.populateFakeDataTime = populateFakeDataTime;
    }

    public long getPopulateToDatabaseTime() {
        return populateToDatabaseTime;
    }

    public void setPopulateToDatabaseTime(long populateToDatabaseTime) {
        this.populateToDatabaseTime = populateToDatabaseTime;
    }

    public long getDumpDatabaseSourceTime() {
        return dumpDatabaseSourceTime;
    }

    public void setDumpDatabaseSourceTime(long dumpDatabaseSourceTime) {
        this.dumpDatabaseSourceTime = dumpDatabaseSourceTime;
    }

    public long getRestoreDatabaseSourceToTargetTime() {
        return restoreDatabaseSourceToTargetTime;
    }

    public void setRestoreDatabaseSourceToTargetTime(long restoreDatabaseSourceToTargetTime) {
        this.restoreDatabaseSourceToTargetTime = restoreDatabaseSourceToTargetTime;
    }

    public long getDumpDatabaseTargetTime() {
        return dumpDatabaseTargetTime;
    }

    public void setDumpDatabaseTargetTime(long dumpDatabaseTargetTime) {
        this.dumpDatabaseTargetTime = dumpDatabaseTargetTime;
    }

    public long getDiffTime() {
        return diffTime;
    }

    public void setDiffTime(long diffTime) {
        this.diffTime = diffTime;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        UUID uuid = UUID.nameUUIDFromBytes(name.getBytes());
        this.id = uuid.toString();
        this.name = name;
    }

    public boolean isSkipped() {
        return skipped;
    }

    public void setSkipped(boolean skipped) {
        this.skipped = skipped;
    }

    public String getSkippedReason() {
        return skippedReason;
    }

    public void setSkippedReason(String skippedReason) {
        this.skippedReason = skippedReason;
    }

    public long getFakeDataFileSize() {
        return fakeDataFileSize;
    }

    public void setFakeDataFileSize(long fakeDataFileSize) {
        this.fakeDataFileSize = fakeDataFileSize;
    }

    public String getId() {
        return id;
    }
}
