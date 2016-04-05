package com.orange.clara.cloud.servicedbdumper.dbdumper.core.dbdrivers;

import com.orange.clara.cloud.servicedbdumper.exception.CannotFindDatabaseDumperException;
import com.orange.clara.cloud.servicedbdumper.model.DatabaseRef;
import com.orange.clara.cloud.servicedbdumper.model.DatabaseType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import javax.annotation.PostConstruct;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * Copyright (C) 2015 Orange
 * <p>
 * This software is distributed under the terms and conditions of the 'Apache-2.0'
 * license which can be found in the file 'LICENSE' in this package distribution
 * or at 'https://opensource.org/licenses/Apache-2.0'.
 * <p>
 * Author: Arthur Halet
 * Date: 08/09/2015
 */
public class DbDumpersFactory {
    private Map<DatabaseType, DatabaseDriver> dbDumpers;

    @Autowired
    @Qualifier("mysqlBinaryDump")
    private File mysqlBinaryDump;

    @Autowired
    @Qualifier("mysqlBinaryRestore")
    private File mysqlBinaryRestore;

    @Autowired
    @Qualifier("postgresBinaryDump")
    private File postgresBinaryDump;

    @Autowired
    @Qualifier("postgresBinaryRestore")
    private File postgresBinaryRestore;

    @Autowired
    @Qualifier("mongodbBinaryDump")
    private File mongodbBinaryDump;

    @Autowired
    @Qualifier("mongodbBinaryRestore")
    private File mongodbBinaryRestore;

    @Autowired
    @Qualifier("redisRutilBinary")
    private File redisRutilBinary;

    @PostConstruct
    public void dbDumpers() {
        dbDumpers = new HashMap<>();
        dbDumpers.put(DatabaseType.MYSQL, new MysqlDatabaseDriver(this.mysqlBinaryDump, this.mysqlBinaryRestore));
        dbDumpers.put(DatabaseType.POSTGRESQL, new PostgresqlDatabaseDriver(this.postgresBinaryDump, this.postgresBinaryRestore));
        dbDumpers.put(DatabaseType.MONGODB, new MongodbDatabaseDriver(this.mongodbBinaryDump, this.mongodbBinaryRestore));
        dbDumpers.put(DatabaseType.REDIS, new RedisDatabaseDriver(this.redisRutilBinary));
    }

    public DatabaseDriver getDatabaseDumper(DatabaseType databaseType) {
        return this.dbDumpers.get(databaseType);
    }

    public DatabaseDriver getDatabaseDumper(DatabaseRef databaseRef) throws CannotFindDatabaseDumperException {
        DatabaseDriver databaseDriver = this.dbDumpers.get(databaseRef.getType());
        if (databaseDriver == null) {
            throw new CannotFindDatabaseDumperException(databaseRef);
        }
        databaseDriver.setDatabaseRef(databaseRef);
        return databaseDriver;
    }
}
