package com.orange.clara.cloud.servicedbdumper.helper;

import com.orange.clara.cloud.servicedbdumper.model.DatabaseDumpFile;
import com.orange.clara.cloud.servicedbdumper.model.DatabaseRef;
import com.orange.clara.cloud.servicedbdumper.model.DbDumperPlan;
import com.orange.clara.cloud.servicedbdumper.model.DbDumperServiceInstance;

/**
 * Copyright (C) 2016 Orange
 * <p>
 * This software is distributed under the terms and conditions of the 'Apache-2.0'
 * license which can be found in the file 'LICENSE' in this package distribution
 * or at 'https://opensource.org/licenses/Apache-2.0'.
 * <p>
 * Author: Arthur Halet
 * Date: 21/02/2016
 */
public class CalculateQuota {

    public static Long calculateFullQuota(DatabaseRef databaseRef) {
        Long size = 0L;
        for (DbDumperServiceInstance dbDumperServiceInstance : databaseRef.getDbDumperServiceInstances()) {
            DbDumperPlan dbDumperPlan = dbDumperServiceInstance.getDbDumperPlan();
            if (dbDumperPlan.getSize() == null) {
                return 0L;
            }
            size += dbDumperPlan.getSize();
        }
        return size;
    }


    public static Float calculateFullPrice(DatabaseRef databaseRef) {
        Float price = 0.0F;
        for (DbDumperServiceInstance dbDumperServiceInstance : databaseRef.getDbDumperServiceInstances()) {
            DbDumperPlan dbDumperPlan = dbDumperServiceInstance.getDbDumperPlan();
            price += dbDumperPlan.getCost();
        }
        return price;
    }

    public static Long calculateQuotaFree(DbDumperServiceInstance dbDumperServiceInstance) {
        DbDumperPlan dbDumperPlan = dbDumperServiceInstance.getDbDumperPlan();
        if (dbDumperPlan.getSize() == null) {
            return 0L;
        }
        Long quota = dbDumperPlan.getSize();
        if (quota == null || quota == 0L) {
            return 0L;
        }
        return quota - calculateDumpFullSize(dbDumperServiceInstance);
    }

    public static Long calculateQuotaFree(DatabaseRef databaseRef) {
        Long fullQuota = calculateFullQuota(databaseRef);
        if (fullQuota == null || fullQuota == 0L) {
            return 0L;
        }
        return fullQuota - calculateDumpFullSize(databaseRef);
    }

    public static Long calculateQuotaUsedInPercent(DbDumperServiceInstance dbDumperServiceInstance) {
        DbDumperPlan dbDumperPlan = dbDumperServiceInstance.getDbDumperPlan();
        if (dbDumperPlan.getSize() == null) {
            return 0L;
        }
        Long quota = dbDumperPlan.getSize();
        Long percent = calculateDumpFullSize(dbDumperServiceInstance) * 100 / quota;
        if (percent > 100) {
            return 100L;
        }
        return percent;
    }

    public static Long calculateQuotaUsedInPercent(DatabaseRef databaseRef) {
        Long fullQuota = calculateFullQuota(databaseRef);
        if (fullQuota == null || fullQuota == 0L) {
            return 0L;
        }
        Long percent = calculateDumpFullSize(databaseRef) * 100 / fullQuota;
        if (percent > 100) {
            return 100L;
        }
        return percent;
    }

    public static Long calculateDumpFullSize(DbDumperServiceInstance dbDumperServiceInstance) {
        Long size = 0L;
        for (DatabaseDumpFile databaseDumpFile : dbDumperServiceInstance.getDatabaseDumpFiles()) {
            size += databaseDumpFile.getSize();
        }
        return size;
    }

    public static Long calculateDumpFullSize(DatabaseRef databaseRef) {
        Long size = 0L;
        for (DbDumperServiceInstance dbDumperServiceInstance : databaseRef.getDbDumperServiceInstances()) {
            size += calculateDumpFullSize(dbDumperServiceInstance);
        }
        return size;
    }
}
