package com.orange.clara.cloud.servicedbdumper.config;

import com.orange.clara.cloud.servicedbdumper.dbdumper.Credentials;
import com.orange.clara.cloud.servicedbdumper.dbdumper.Deleter;
import com.orange.clara.cloud.servicedbdumper.dbdumper.Dumper;
import com.orange.clara.cloud.servicedbdumper.dbdumper.Restorer;
import com.orange.clara.cloud.servicedbdumper.dbdumper.core.CoreCredentials;
import com.orange.clara.cloud.servicedbdumper.dbdumper.core.CoreDeleter;
import com.orange.clara.cloud.servicedbdumper.dbdumper.core.CoreDumper;
import com.orange.clara.cloud.servicedbdumper.dbdumper.core.CoreRestorer;
import com.orange.clara.cloud.servicedbdumper.dbdumper.core.dbdrivers.DbDumpersFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 * Copyright (C) 2015 Orange
 * <p>
 * This software is distributed under the terms and conditions of the 'Apache-2.0'
 * license which can be found in the file 'LICENSE' in this package distribution
 * or at 'https://opensource.org/licenses/Apache-2.0'.
 * <p>
 * Author: Arthur Halet
 * Date: 03/06/2015
 */
@Configuration
@Profile({"core", "local"})
public class DbCoreDumperConfig {


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
    public Credentials credentials() {
        return new CoreCredentials();
    }

    @Bean
    public Deleter deleter() {
        return new CoreDeleter();
    }

}
