package com.orange.clara.cloud.config;

import com.orange.clara.cloud.dbdump.DbDumpersFactory;
import com.orange.clara.cloud.dbdump.action.Dumper;
import com.orange.clara.cloud.dbdump.s3.UploadS3Stream;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Copyright (C) 2015 Orange
 * <p/>
 * This software is distributed under the terms and conditions of the 'MIT'
 * license which can be found in the file 'LICENSE' in this package distribution
 * or at 'http://opensource.org/licenses/MIT'.
 * <p/>
 * Author: Arthur Halet
 * Date: 03/06/2015
 */
@Configuration
public class DbDumperConfig {

    @Bean
    public DbDumpersFactory dbDumpersFactory() {
        return new DbDumpersFactory();
    }

    @Bean
    public Dumper dumper() {
        return new Dumper();
    }

    @Bean
    public UploadS3Stream uploadS3Stream() {
        return new UploadS3Stream();
    }
}
