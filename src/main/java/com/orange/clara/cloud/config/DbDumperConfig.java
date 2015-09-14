package com.orange.clara.cloud.config;

import com.orange.clara.cloud.dbdump.*;
import com.orange.clara.cloud.dbdump.action.Dumper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

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
}
