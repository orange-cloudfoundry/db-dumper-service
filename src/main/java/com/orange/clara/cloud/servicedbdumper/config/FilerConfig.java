package com.orange.clara.cloud.servicedbdumper.config;

import com.google.common.collect.Lists;
import com.orange.clara.cloud.servicedbdumper.filer.Filer;
import com.orange.clara.cloud.servicedbdumper.filer.factory.FactoryFiler;
import com.orange.clara.cloud.servicedbdumper.filer.factory.FilerType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;

import java.util.List;

/**
 * Copyright (C) 2016 Orange
 * <p>
 * This software is distributed under the terms and conditions of the 'Apache-2.0'
 * license which can be found in the file 'LICENSE' in this package distribution
 * or at 'https://opensource.org/licenses/Apache-2.0'.
 * <p>
 * Author: Arthur Halet
 * Date: 26/02/2016
 */
@Configuration
@Profile({"local", "core"})
public class FilerConfig {

    @Value("${filer.type:#{null}}")
    private String filerType;

    @Autowired
    private Environment env;

    @Bean
    public Filer filer() throws InstantiationException, IllegalAccessException {
        if (filerType != null) {
            return FactoryFiler.createFiler(this.filerType);
        }
        List<String> profiles = Lists.newArrayList(env.getActiveProfiles());
        if (profiles.contains("local") && !profiles.contains("s3")) {
            return FactoryFiler.createFiler(FilerType.GZIPDISK);
        }
        return FactoryFiler.createFiler(FilerType.GZIPS3);
    }
}
