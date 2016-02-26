package com.orange.clara.cloud.servicedbdumper.dbdumper.core.dbdrivers;

import com.orange.clara.cloud.servicedbdumper.exception.CannotFindDatabaseDumperException;
import com.orange.clara.cloud.servicedbdumper.exception.DatabaseExtractionException;
import com.orange.clara.cloud.servicedbdumper.model.DatabaseRef;
import com.orange.clara.cloud.servicedbdumper.model.DatabaseType;
import org.junit.Before;
import org.junit.Test;

import java.net.URI;

import static org.fest.assertions.Assertions.assertThat;
import static org.fest.assertions.Fail.fail;

/**
 * Copyright (C) 2016 Arthur Halet
 * <p>
 * This software is distributed under the terms and conditions of the 'MIT'
 * license which can be found in the file 'LICENSE' in this package distribution
 * or at 'http://opensource.org/licenses/MIT'.
 * <p>
 * Author: Arthur Halet
 * Date: 26/02/2016
 */
public class DbDumpersFactoryTest {
    private DbDumpersFactory dbDumpersFactory = new DbDumpersFactory();

    @Before
    public void init() {
        dbDumpersFactory.dbDumpers();
    }

    @Test
    public void pass_databasetype_and_return_correct_databasedriver() {
        this.assertIsCorrectDatabaseDriver(dbDumpersFactory.getDatabaseDumper(DatabaseType.MONGODB), MongodbDatabaseDriver.class);
        this.assertIsCorrectDatabaseDriver(dbDumpersFactory.getDatabaseDumper(DatabaseType.MYSQL), MysqlDatabaseDriver.class);
        this.assertIsCorrectDatabaseDriver(dbDumpersFactory.getDatabaseDumper(DatabaseType.POSTGRESQL), PostgresqlDatabaseDriver.class);
        this.assertIsCorrectDatabaseDriver(dbDumpersFactory.getDatabaseDumper(DatabaseType.REDIS), RedisDatabaseDriver.class);

    }

    @Test
    public void pass_databaseref_and_return_correct_databasedriver() throws CannotFindDatabaseDumperException, DatabaseExtractionException {
        DatabaseDriver databaseDriver = dbDumpersFactory.getDatabaseDumper(new DatabaseRef("mydb", URI.create("mongo://test.com/mydb")));
        this.assertIsCorrectDatabaseDriver(databaseDriver, MongodbDatabaseDriver.class);
        databaseDriver = dbDumpersFactory.getDatabaseDumper(new DatabaseRef("mydb", URI.create("mysql://test.com/mydb")));
        this.assertIsCorrectDatabaseDriver(databaseDriver, MysqlDatabaseDriver.class);
        databaseDriver = dbDumpersFactory.getDatabaseDumper(new DatabaseRef("mydb", URI.create("postgres://test.com/mydb")));
        this.assertIsCorrectDatabaseDriver(databaseDriver, PostgresqlDatabaseDriver.class);
        databaseDriver = dbDumpersFactory.getDatabaseDumper(new DatabaseRef("mydb", URI.create("redis://test.com/mydb")));
        this.assertIsCorrectDatabaseDriver(databaseDriver, RedisDatabaseDriver.class);

    }

    private void assertIsCorrectDatabaseDriver(DatabaseDriver databaseDriver, Class<?> type) {
        assertThat(databaseDriver).isNotNull();
        assertThat(databaseDriver).isInstanceOf(type);
    }

    @Test
    public void pass_database_ref_with_null_database_type_raise_error() throws DatabaseExtractionException {
        DatabaseRef databaseRef = new DatabaseRef("mydb", URI.create("mongo://test.com/mydb"));
        databaseRef.setType(null);
        try {
            dbDumpersFactory.getDatabaseDumper(databaseRef);
            fail("Should throw an CannotFindDatabaseDumperException");
        } catch (Exception e) {
            assertThat(e).isInstanceOf(CannotFindDatabaseDumperException.class);
        }
    }
}