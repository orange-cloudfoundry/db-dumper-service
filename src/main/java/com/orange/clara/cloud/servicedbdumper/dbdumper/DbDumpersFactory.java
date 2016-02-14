package com.orange.clara.cloud.servicedbdumper.dbdumper;

import com.orange.clara.cloud.servicedbdumper.exception.CannotFindDatabaseDumperException;
import com.orange.clara.cloud.servicedbdumper.model.DatabaseRef;
import com.orange.clara.cloud.servicedbdumper.model.DatabaseType;
import org.springframework.beans.factory.annotation.Value;

import javax.annotation.PostConstruct;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * Copyright (C) 2015 Orange
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

    @PostConstruct
    public void dbDumpers() {
        dbDumpers = new HashMap<>();
        dbDumpers.put(DatabaseType.MYSQL, new MysqlDatabaseDumper(this.mysqlBinaryDump, this.mysqlBinaryRestore));
        dbDumpers.put(DatabaseType.POSTGRESQL, new PostgresqlDatabaseDumper(this.postgresBinaryDump, this.postgresBinaryRestore));
        dbDumpers.put(DatabaseType.MONGODB, new MongodbDatabaseDumper(this.mongodbBinaryDump, this.mongodbBinaryRestore));
        dbDumpers.put(DatabaseType.REDIS, new RedisDatabaseDumper(this.redisRutilBinary));
    }

    public DatabaseDumper getDatabaseDumper(DatabaseType databaseType) {
        return this.dbDumpers.get(databaseType);
    }

    public DatabaseDumper getDatabaseDumper(DatabaseRef databaseRef) throws CannotFindDatabaseDumperException {
        DatabaseDumper databaseDumper = this.dbDumpers.get(databaseRef.getType());
        if (databaseDumper == null) {
            throw new CannotFindDatabaseDumperException(databaseRef);
        }
        databaseDumper.setDatabaseRef(databaseRef);
        return databaseDumper;
    }
}
