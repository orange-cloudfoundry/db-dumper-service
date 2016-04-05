package com.orange.clara.cloud.servicedbdumper.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.io.File;

/**
 * Copyright (C) 2016 Orange
 * <p>
 * This software is distributed under the terms and conditions of the 'Apache-2.0'
 * license which can be found in the file 'LICENSE' in this package distribution
 * or at 'https://opensource.org/licenses/Apache-2.0'.
 * <p>
 * Author: Arthur Halet
 * Date: 05/04/2016
 */
@Configuration
@Profile({"core", "local"})
public class BinariesPathConfig {

    @Value("${mysql.dump.bin.path:classpath:binaries/mysql/bin/mysqldump}")
    private File mysqlBinaryDump;

    @Value("${mysql.restore.bin.path:classpath:binaries/mysql/bin/mysql}")
    private File mysqlBinaryRestore;

    @Value("${postgres.dump.bin.path:classpath:binaries/postgresql/bin/pg_dump}")
    private File postgresBinaryDump;

    @Value("${postgres.restore.bin.path:classpath:binaries/postgresql/bin/psql}")
    private File postgresBinaryRestore;

    @Value("${mongodb.dump.bin.path:classpath:binaries/mongodb/bin/mongodump}")
    private File mongodbBinaryDump;

    @Value("${mongodb.restore.bin.path:classpath:binaries/mongodb/bin/mongorestore}")
    private File mongodbBinaryRestore;

    @Value("${redis.rutil.bin.path:classpath:binaries/redis/bin/rutil}")
    private File redisRutilBinary;

    @Bean
    public File mysqlBinaryDump() {
        return mysqlBinaryDump;
    }

    @Bean
    public File mysqlBinaryRestore() {
        return mysqlBinaryRestore;
    }

    @Bean
    public File postgresBinaryDump() {
        return postgresBinaryDump;
    }

    @Bean
    public File postgresBinaryRestore() {
        return postgresBinaryRestore;
    }

    @Bean
    public File mongodbBinaryDump() {
        return mongodbBinaryDump;
    }

    @Bean
    public File mongodbBinaryRestore() {
        return mongodbBinaryRestore;
    }

    @Bean
    public File redisRutilBinary() {
        return redisRutilBinary;
    }
}
