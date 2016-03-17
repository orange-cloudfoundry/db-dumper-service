package com.orange.clara.cloud.servicedbdumper.helper;

import com.google.common.collect.Lists;
import com.orange.clara.cloud.servicedbdumper.model.DatabaseDumpFile;
import com.orange.clara.cloud.servicedbdumper.model.DatabaseRef;
import com.orange.clara.cloud.servicedbdumper.model.DbDumperPlan;
import com.orange.clara.cloud.servicedbdumper.model.DbDumperServiceInstance;
import org.junit.Before;
import org.junit.Test;

import static com.orange.clara.cloud.servicedbdumper.helper.CalculateQuota.*;
import static org.fest.assertions.Assertions.assertThat;

/**
 * Copyright (C) 2016 Arthur Halet
 * <p>
 * This software is distributed under the terms and conditions of the 'MIT'
 * license which can be found in the file 'LICENSE' in this package distribution
 * or at 'http://opensource.org/licenses/MIT'.
 * <p>
 * Author: Arthur Halet
 * Date: 17/03/2016
 */
public class CalculateQuotaTest {

    private final static Long planSize = 1024L;
    private final static Float cost = 1.0F;
    private final static Long size = 100L;

    private final static DbDumperPlan dbDumperPlan = new DbDumperPlan("1", "1ko", planSize);
    private final static DatabaseRef databaseRef = new DatabaseRef();
    private final static DbDumperServiceInstance dbDumperServiceInstance1 = new DbDumperServiceInstance("1", "fakeplanid", "fakeorgguid", "fakespaceguid", "", null);
    private final static DbDumperServiceInstance dbDumperServiceInstance2 = new DbDumperServiceInstance("2", "fakeplanid", "fakeorgguid", "fakespaceguid", "", null);
    private final static DatabaseDumpFile databaseDumpFile1 = new DatabaseDumpFile();
    private final static DatabaseDumpFile databaseDumpFile2 = new DatabaseDumpFile();

    @Before
    public void init() {
        dbDumperServiceInstance1.setDbDumperPlan(dbDumperPlan);
        dbDumperServiceInstance2.setDbDumperPlan(dbDumperPlan);
        dbDumperServiceInstance1.setDatabaseRef(databaseRef);
        dbDumperServiceInstance2.setDatabaseRef(databaseRef);
        databaseDumpFile1.setSize(size);
        databaseDumpFile2.setSize(size);
        databaseDumpFile1.setId(1);
        databaseDumpFile2.setId(2);
        databaseRef.addDatabaseDumpFile(databaseDumpFile1);
        databaseRef.addDatabaseDumpFile(databaseDumpFile2);
        dbDumperPlan.setSize(planSize);
        dbDumperPlan.setCost(cost);
    }

    @Test
    public void testCalculateFullQuota() throws Exception {
        assertThat(calculateFullQuota(databaseRef)).isEqualTo(2048);

        databaseRef.setDbDumperServiceInstances(Lists.newArrayList());
        assertThat(calculateFullQuota(databaseRef)).isEqualTo(0L);
    }

    @Test
    public void testCalculateFullPrice() throws Exception {
        assertThat(calculateFullPrice(databaseRef)).isEqualTo(2);

        databaseRef.setDbDumperServiceInstances(Lists.newArrayList());
        assertThat(calculateFullPrice(databaseRef)).isEqualTo(0F);
    }

    @Test
    public void testCalculateQuotaFreeFromDatabaseRef() throws Exception {
        assertThat(calculateQuotaFree(databaseRef)).isEqualTo(1848);

        databaseRef.setDbDumperServiceInstances(Lists.newArrayList());
        assertThat(calculateQuotaFree(databaseRef)).isEqualTo(0L);
    }

    @Test
    public void testCalculateQuotaFreeFromDbServiceInstance() throws Exception {
        assertThat(calculateQuotaFree(dbDumperServiceInstance1)).isEqualTo(1848);

        databaseRef.setDbDumperServiceInstances(Lists.newArrayList());
        assertThat(calculateQuotaFree(dbDumperServiceInstance1)).isEqualTo(0L);
    }

    @Test
    public void testCalculateQuotaUsedInPercentFromDatabaseRef() throws Exception {
        assertThat(calculateQuotaUsedInPercent(databaseRef)).isEqualTo(9);

        databaseRef.setDbDumperServiceInstances(Lists.newArrayList());
        assertThat(calculateQuotaUsedInPercent(databaseRef)).isEqualTo(0);
    }

    @Test
    public void testCalculateQuotaUsedInPercentFromDbServiceInstance() throws Exception {
        assertThat(calculateQuotaUsedInPercent(dbDumperServiceInstance1)).isEqualTo(9);

        databaseRef.setDbDumperServiceInstances(Lists.newArrayList());
        assertThat(calculateQuotaUsedInPercent(dbDumperServiceInstance1)).isEqualTo(0);
    }

    @Test
    public void testCalculateDumpFullSizeFromDatabaseRef() throws Exception {
        assertThat(calculateDumpFullSize(databaseRef)).isEqualTo(200);

        databaseRef.setDbDumperServiceInstances(Lists.newArrayList());
        assertThat(calculateQuotaUsedInPercent(databaseRef)).isEqualTo(0);
    }

    @Test
    public void testCalculateDumpFullSizeFromDbServiceInstance() throws Exception {
        assertThat(calculateDumpFullSize(dbDumperServiceInstance1)).isEqualTo(200);

        databaseRef.setDbDumperServiceInstances(Lists.newArrayList());
        assertThat(calculateQuotaUsedInPercent(dbDumperServiceInstance1)).isEqualTo(0);
    }
}