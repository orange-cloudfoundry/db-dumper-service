package com.orange.clara.cloud.servicedbdumper.dbdumper.core;

import com.orange.clara.cloud.servicedbdumper.dbdumper.core.dbdrivers.DatabaseDriver;
import com.orange.clara.cloud.servicedbdumper.dbdumper.core.dbdrivers.DbDumpersFactory;
import com.orange.clara.cloud.servicedbdumper.fake.filer.EchoFiler;
import com.orange.clara.cloud.servicedbdumper.fake.databasedrivers.EchoDatabaseDriver;
import com.orange.clara.cloud.servicedbdumper.exception.CannotFindDatabaseDumperException;
import com.orange.clara.cloud.servicedbdumper.exception.DatabaseExtractionException;
import com.orange.clara.cloud.servicedbdumper.filer.Filer;
import com.orange.clara.cloud.servicedbdumper.model.DatabaseDumpFile;
import com.orange.clara.cloud.servicedbdumper.model.DatabaseRef;
import com.orange.clara.cloud.servicedbdumper.repo.DatabaseDumpFileRepo;
import com.orange.clara.cloud.servicedbdumper.repo.DatabaseRefRepo;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import static org.fest.assertions.Assertions.assertThat;
import static org.mockito.Matchers.notNull;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

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
public class CoreDeleterTest extends AbstractCoreTester {
    public static final String DB_URI = "mysql://mydb.com/database";

    @InjectMocks
    CoreDeleter coreDeleter;


    @Mock
    DbDumpersFactory dbDumpersFactory;


    @Mock
    DatabaseDumpFileRepo databaseDumpFileRepo;

    @Mock
    DatabaseRefRepo databaseRefRepo;

    DatabaseRef databaseRef;

    DatabaseDumpFile databaseDumpFile;

    @Before
    public void init() throws DatabaseExtractionException {
        initMocks(this);
        databaseRef = this.generateDatabaseRef(DB_URI);
        databaseDumpFile = this.generateDatabaseDumpFile(1, databaseRef);
        this.generateDatabaseDumpFile(2, databaseRef);
        assertThat(databaseRef.getDatabaseDumpFiles()).hasSize(2);
        DatabaseDriver databaseDriver = new EchoDatabaseDriver();
        Filer filer = new EchoFiler("testit");
        this.coreDeleter.filer = filer;
        databaseDriver.setDatabaseRef(databaseRef);
    }

    @Test
    public void delete_database_dump_from_database_ref_remove_all_database_dump_from_database_ref() throws CannotFindDatabaseDumperException {
        when(databaseRefRepo.save((DatabaseRef) notNull())).thenReturn(null);
        this.coreDeleter.deleteAll(databaseRef);
        assertThat(databaseRef.getDatabaseDumpFiles()).hasSize(0);
    }

    @Test
    public void delete_database_dump_remove_dump_from_database_ref() throws CannotFindDatabaseDumperException {
        when(databaseRefRepo.save((DatabaseRef) notNull())).thenReturn(null);
        this.coreDeleter.delete(databaseDumpFile);
        assertThat(databaseRef.getDatabaseDumpFiles()).hasSize(1);
    }
}