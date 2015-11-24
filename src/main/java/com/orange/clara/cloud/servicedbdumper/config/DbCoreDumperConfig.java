package com.orange.clara.cloud.servicedbdumper.config;

import com.orange.clara.cloud.servicedbdumper.dbdumper.DbDumpersFactory;
import com.orange.clara.cloud.servicedbdumper.dbdumper.running.Dumper;
import com.orange.clara.cloud.servicedbdumper.dbdumper.running.Restorer;
import com.orange.clara.cloud.servicedbdumper.dbdumper.running.core.CoreDumper;
import com.orange.clara.cloud.servicedbdumper.dbdumper.running.core.CoreRestorer;
import com.orange.clara.cloud.servicedbdumper.dbdumper.s3.UploadS3Stream;
import com.orange.clara.cloud.servicedbdumper.dbdumper.s3.UploadS3StreamImpl;
import com.orange.clara.cloud.servicedbdumper.filer.Filer;
import com.orange.clara.cloud.servicedbdumper.filer.S3Filer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

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
@Profile("core")
public class DbCoreDumperConfig {

    @Bean
    public Filer filer() {
        return new S3Filer();
    }

    @Bean
    public DbDumpersFactory dbDumpersFactory() {
        return new DbDumpersFactory();
    }

    @Bean
    public Dumper dumper() {
        return new CoreDumper();
    }

    @Bean
    public Restorer restorer() {
        return new CoreRestorer();
    }

    @Bean
    public UploadS3Stream uploadS3Stream() {
        return new UploadS3StreamImpl();
    }
}
