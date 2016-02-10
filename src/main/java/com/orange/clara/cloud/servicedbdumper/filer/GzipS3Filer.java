package com.orange.clara.cloud.servicedbdumper.filer;

/**
 * Copyright (C) 2016 Arthur Halet
 * <p/>
 * This software is distributed under the terms and conditions of the 'MIT'
 * license which can be found in the file 'LICENSE' in this package distribution
 * or at 'http://opensource.org/licenses/MIT'.
 * <p/>
 * Author: Arthur Halet
 * Date: 09/02/2016
 */
public class GzipS3Filer extends AbstractGzipGenericFiler implements Filer {

    public GzipS3Filer() {
        super(new S3Filer());
    }
}
