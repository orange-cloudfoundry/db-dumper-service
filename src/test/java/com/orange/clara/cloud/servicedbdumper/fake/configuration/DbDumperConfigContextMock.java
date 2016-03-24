package com.orange.clara.cloud.servicedbdumper.fake.configuration;

import com.orange.clara.cloud.servicedbdumper.dbdumper.Deleter;
import com.orange.clara.cloud.servicedbdumper.fake.dbdumper.mocked.DeleterMock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Copyright (C) 2016 Orange
 * <p>
 * This software is distributed under the terms and conditions of the 'Apache-2.0'
 * license which can be found in the file 'LICENSE' in this package distribution
 * or at 'https://opensource.org/licenses/Apache-2.0'.
 * <p>
 * Author: Arthur Halet
 * Date: 24/03/2016
 */
@Configuration
public class DbDumperConfigContextMock {
    @Bean
    public Deleter deleter() {
        return new DeleterMock();
    }
}
