package com.orange.clara.cloud.servicedbdumper.filer;

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
public class GzipDiskFiler extends AbstractGzipGenericFiler implements Filer {
    public GzipDiskFiler() {
        super(new DiskFiler());
    }
}
