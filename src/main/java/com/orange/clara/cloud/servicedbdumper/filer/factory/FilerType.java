package com.orange.clara.cloud.servicedbdumper.filer.factory;

import com.orange.clara.cloud.servicedbdumper.filer.*;

/**
 * Copyright (C) 2016 Orange
 * <p/>
 * This software is distributed under the terms and conditions of the 'Apache-2.0'
 * license which can be found in the file 'LICENSE' in this package distribution
 * or at 'https://opensource.org/licenses/Apache-2.0'.
 * <p/>
 * Author: Arthur Halet
 * Date: 10/02/2016
 */
public enum FilerType {
    GZIPS3(GzipS3Filer.class), S3(S3Filer.class), DISK(DiskFiler.class), GZIPDISK(GzipDiskFiler.class);

    private Class<? extends Filer> clazz;

    FilerType(Class<? extends Filer> clazz) {
        this.clazz = clazz;
    }


    public Class<? extends Filer> getClazz() {
        return clazz;
    }
}
