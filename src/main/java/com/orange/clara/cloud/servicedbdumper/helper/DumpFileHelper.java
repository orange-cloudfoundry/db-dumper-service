package com.orange.clara.cloud.servicedbdumper.helper;

import com.orange.clara.cloud.servicedbdumper.model.DatabaseDumpFile;
import com.orange.clara.cloud.servicedbdumper.model.DatabaseRef;

/**
 * Copyright (C) 2016 Orange
 * <p>
 * This software is distributed under the terms and conditions of the 'Apache-2.0'
 * license which can be found in the file 'LICENSE' in this package distribution
 * or at 'https://opensource.org/licenses/Apache-2.0'.
 * <p>
 * Author: Arthur Halet
 * Date: 21/09/2016
 */
public class DumpFileHelper {

    public static String getFilePath(DatabaseDumpFile databaseDumpFile) {
        return databaseDumpFile.getDbDumperServiceInstance().getDatabaseRef().getName() + "/" + databaseDumpFile.getFileName();
    }

    public static String getFilePath(DatabaseRef databaseRef, String filename) {
        return databaseRef.getName() + "/" + filename;
    }
}
