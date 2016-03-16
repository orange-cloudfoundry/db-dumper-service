package com.orange.clara.cloud.servicedbdumper.filer.factory;

import com.orange.clara.cloud.servicedbdumper.filer.*;
import org.junit.Test;

import static org.fest.assertions.Assertions.assertThat;

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
public class FactoryFilerTest {

    @Test
    public void pass_filer_type_and_return_correct_filer_implementation() throws InstantiationException, IllegalAccessException {
        this.assertIsCorrectFiler(FactoryFiler.createFiler(FilerType.GZIPS3), GzipS3Filer.class);
        this.assertIsCorrectFiler(FactoryFiler.createFiler(FilerType.DISK), DiskFiler.class);
        this.assertIsCorrectFiler(FactoryFiler.createFiler(FilerType.GZIPDISK), GzipDiskFiler.class);
        this.assertIsCorrectFiler(FactoryFiler.createFiler(FilerType.S3), S3Filer.class);
    }

    @Test
    public void pass_filer_name_and_return_correct_filer_implementation() throws InstantiationException, IllegalAccessException {
        this.assertIsCorrectFiler(FactoryFiler.createFiler("gZipS3"), GzipS3Filer.class);
        this.assertIsCorrectFiler(FactoryFiler.createFiler("DISK"), DiskFiler.class);
        this.assertIsCorrectFiler(FactoryFiler.createFiler("gzipdisk"), GzipDiskFiler.class);
        this.assertIsCorrectFiler(FactoryFiler.createFiler("s3"), S3Filer.class);
    }

    @Test
    public void when_giving_a_wrong_filer_it_should_default_to_gzips3() throws InstantiationException, IllegalAccessException {
        this.assertIsCorrectFiler(FactoryFiler.createFiler("foo"), GzipS3Filer.class);
    }

    private void assertIsCorrectFiler(Filer filer, Class<?> type) {
        assertThat(filer).isNotNull();
        assertThat(filer).isInstanceOf(type);
    }
}