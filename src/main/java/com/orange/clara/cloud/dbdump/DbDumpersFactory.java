package com.orange.clara.cloud.dbdump;

import com.orange.clara.cloud.dbdump.DatabaseDumper;
import com.orange.clara.cloud.dbdump.MongodbDatabaseDumper;
import com.orange.clara.cloud.dbdump.MysqlDatabaseDumper;
import com.orange.clara.cloud.dbdump.PostgresqlDatabaseDumper;
import com.orange.clara.cloud.model.DatabaseType;
import org.springframework.beans.factory.annotation.Value;

import javax.annotation.PostConstruct;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * Copyright (C) 2015 Arthur Halet
 * <p/>
 * This software is distributed under the terms and conditions of the 'MIT'
 * license which can be found in the file 'LICENSE' in this package distribution
 * or at 'http://opensource.org/licenses/MIT'.
 * <p/>
 * Author: Arthur Halet
 * Date: 08/09/2015
 */
public class DbDumpersFactory {
    private Map<DatabaseType, DatabaseDumper> dbDumpers;

    @Value("classpath:binaries/mysql/bin/mysqldump")
    private File mysqlBinaryDump;
    @Value("classpath:binaries/mysql/bin/mysql")
    private File mysqlBinaryRestore;
    @Value("classpath:binaries/postgresql/bin/pg_dump")
    private File postgresBinaryDump;
    @Value("classpath:binaries/postgresql/bin/psql")
    private File postgresBinaryRestore;
    @Value("classpath:binaries/mongodb/bin/mongodump")
    private File mongodbBinaryDump;
    @Value("classpath:binaries/mongodb/bin/mongorestore")
    private File mongodbBinaryRestore;


    @PostConstruct
    public void dbDumpers() {
        dbDumpers = new HashMap<>();
        dbDumpers.put(DatabaseType.MYSQL, new MysqlDatabaseDumper(this.mysqlBinaryDump, this.mysqlBinaryRestore));
        dbDumpers.put(DatabaseType.POSTGRESQL, new PostgresqlDatabaseDumper(this.postgresBinaryDump, this.postgresBinaryRestore));
        dbDumpers.put(DatabaseType.MONGODB, new MongodbDatabaseDumper(this.mongodbBinaryDump, this.mongodbBinaryRestore));
    }

    public DatabaseDumper getDatabaseDumper(DatabaseType databaseType) {
        return this.dbDumpers.get(databaseType);
    }
}
