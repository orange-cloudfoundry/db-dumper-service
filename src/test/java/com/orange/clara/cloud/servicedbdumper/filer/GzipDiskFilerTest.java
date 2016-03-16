package com.orange.clara.cloud.servicedbdumper.filer;

import com.orange.clara.cloud.servicedbdumper.filer.compression.GzipCompressing;
import org.junit.Before;

/**
 * Copyright (C) 2016 Arthur Halet
 * <p>
 * This software is distributed under the terms and conditions of the 'MIT'
 * license which can be found in the file 'LICENSE' in this package distribution
 * or at 'http://opensource.org/licenses/MIT'.
 * <p>
 * Author: Arthur Halet
 * Date: 14/03/2016
 */
public class GzipDiskFilerTest extends DiskFilerTest {

    @Before
    public void init() {
        GzipDiskFiler gzipDiskFiler = new GzipDiskFiler();
        gzipDiskFiler.gzipCompressing = new GzipCompressing();
        this.filer = gzipDiskFiler;
        this.toCompressedRawContent();
    }

}