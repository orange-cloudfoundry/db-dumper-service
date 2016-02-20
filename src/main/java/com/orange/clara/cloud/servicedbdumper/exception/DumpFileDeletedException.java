package com.orange.clara.cloud.servicedbdumper.exception;

import com.orange.clara.cloud.servicedbdumper.model.DatabaseDumpFile;

/**
 * Copyright (C) 2016 Orange
 * <p>
 * This software is distributed under the terms and conditions of the 'Apache-2.0'
 * license which can be found in the file 'LICENSE' in this package distribution
 * or at 'https://opensource.org/licenses/Apache-2.0'.
 * <p>
 * Author: Arthur Halet
 * Date: 20/02/2016
 */
public class DumpFileDeletedException extends Exception {
    public DumpFileDeletedException(DatabaseDumpFile databaseDumpFile) {
        super("Dump file '" + databaseDumpFile.getFileName() + "' has been deleted.");
    }
}