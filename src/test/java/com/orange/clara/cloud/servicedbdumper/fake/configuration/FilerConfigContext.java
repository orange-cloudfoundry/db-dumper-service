package com.orange.clara.cloud.servicedbdumper.fake.configuration;

import com.orange.clara.cloud.servicedbdumper.fake.filer.EchoFiler;
import com.orange.clara.cloud.servicedbdumper.filer.Filer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

/**
 * Copyright (C) 2016 Orange
 * <p>
 * This software is distributed under the terms and conditions of the 'Apache-2.0'
 * license which can be found in the file 'LICENSE' in this package distribution
 * or at 'https://opensource.org/licenses/Apache-2.0'.
 * <p>
 * Author: Arthur Halet
 * Date: 23/03/2016
 */
@Configuration
@Profile("test-controller")
public class FilerConfigContext {
    @Bean
    public Filer filer() throws InstantiationException, IllegalAccessException {
        return new EchoFiler(this.testTextForFiler());
    }

    @Bean
    @Primary
    public String testTextForFiler() {
        return "my test";
    }
}
