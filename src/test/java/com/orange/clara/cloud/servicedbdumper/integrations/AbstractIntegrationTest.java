package com.orange.clara.cloud.servicedbdumper.integrations;

import com.orange.clara.cloud.servicedbdumper.dbdumper.core.dbdrivers.DbDumpersFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import java.io.File;

/**
 * Copyright (C) 2016 Orange
 * <p>
 * This software is distributed under the terms and conditions of the 'Apache-2.0'
 * license which can be found in the file 'LICENSE' in this package distribution
 * or at 'https://opensource.org/licenses/Apache-2.0'.
 * <p>
 * Author: Arthur Halet
 * Date: 25/03/2016
 */
abstract public class AbstractIntegrationTest {

    @Autowired
    protected DbDumpersFactory dbDumpersFactory;
    @Value("${classpath:data/fake-data-mongodb.bson}")
    private File mongodbFakeData;
    @Value("${classpath:data/fake-data-mysql.sql}")
    private File mysqlFakeData;
    @Value("${classpath:data/fake-data-redis.rdmp}")
    private File redisFakeData;
    @Value("${classpath:data/fake-data-postgres.sql}")
    private File postgresFakeData;

    abstract public void when_dump_and_restore_a_MYSQL_database_it_should_have_the_database_source_equals_to_the_database_target();

    abstract public void when_dump_and_restore_a_POSTGRES_database_it_should_have_the_database_source_equals_to_the_database_target();

    abstract public void when_dump_and_restore_a_REDIS_database_it_should_have_the_database_source_equals_to_the_database_target();

    abstract public void when_dump_and_restore_a_MONGODB_database_it_should_have_the_database_source_equals_to_the_database_target();


}
