package com.orange.clara.cloud.servicedbdumper.dbdumper.core;

import com.orange.clara.cloud.servicedbdumper.dbdumper.core.dbdrivers.DatabaseDriver;
import com.orange.clara.cloud.servicedbdumper.dbdumper.core.dbdrivers.DbDumpersFactory;
import com.orange.clara.cloud.servicedbdumper.dbdumper.fake.EchoFiler;
import com.orange.clara.cloud.servicedbdumper.dbdumper.fake.databasedrivers.EchoDatabaseDriver;
import com.orange.clara.cloud.servicedbdumper.dbdumper.fake.databasedrivers.ErroredDatabaseDriver;
import com.orange.clara.cloud.servicedbdumper.exception.CannotFindDatabaseDumperException;
import com.orange.clara.cloud.servicedbdumper.exception.DatabaseExtractionException;
import com.orange.clara.cloud.servicedbdumper.exception.RestoreException;
import com.orange.clara.cloud.servicedbdumper.exception.RunProcessException;
import com.orange.clara.cloud.servicedbdumper.filer.Filer;
import com.orange.clara.cloud.servicedbdumper.model.DatabaseDumpFile;
import com.orange.clara.cloud.servicedbdumper.model.DatabaseRef;
import com.orange.clara.cloud.servicedbdumper.repo.DatabaseDumpFileRepo;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.util.Date;

import static org.fest.assertions.Assertions.assertThat;
import static org.fest.assertions.Fail.fail;
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
public class CoreRestorerTest extends AbstractCoreTester {
    public static final String DB_URI = "mysql://mydb.com/database";

    @InjectMocks
    CoreRestorer coreRestorer;


    @Mock
    DbDumpersFactory dbDumpersFactory;


    @Mock
    DatabaseDumpFileRepo databaseDumpFileRepo;

    DatabaseRef databaseRef;

    DatabaseDumpFile databaseDumpFile;

    @Before
    public void init() throws DatabaseExtractionException, CannotFindDatabaseDumperException {
        initMocks(this);
        databaseRef = this.generateDatabaseRef(DB_URI);
        databaseDumpFile = this.generateDatabaseDumpFile(databaseRef);
        DatabaseDriver databaseDriver = new EchoDatabaseDriver();

        databaseDriver.setDatabaseRef(databaseRef);
        Filer filer = new EchoFiler("testit");
        this.coreRestorer.dateFormat = "dd-MM-yyyy HH:mm";
        this.coreRestorer.filer = filer;
        when(dbDumpersFactory.getDatabaseDumper(databaseRef)).thenReturn(databaseDriver);

    }

    @Test
    public void ensure_no_error_is_thrown_when_restore_with_no_date() throws RestoreException {
        when(databaseDumpFileRepo.findFirstByDatabaseRefOrderByCreatedAtDesc(databaseRef)).thenReturn(databaseDumpFile);

        this.coreRestorer.restore(databaseRef, databaseRef);
    }

    @Test
    public void ensure_no_error_is_thrown_when_restore_with_date() throws CannotFindDatabaseDumperException, RestoreException {
        Date date = new Date();
        when(databaseDumpFileRepo.findFirstByDatabaseRefAndCreatedAtGreaterThanEqualOrderByCreatedAtAsc(databaseRef, date)).thenReturn(databaseDumpFile);
        this.coreRestorer.restore(databaseRef, databaseRef, date);
    }

    @Test
    public void restore_goes_in_error_if_databases_types_dont_match() throws CannotFindDatabaseDumperException, DatabaseExtractionException {
        DatabaseRef databaseRefTarget = this.generateDatabaseRef("postgres://mydb.com/database");
        when(databaseDumpFileRepo.findFirstByDatabaseRefOrderByCreatedAtDesc(databaseRef)).thenReturn(databaseDumpFile);
        try {
            this.coreRestorer.restore(databaseRef, databaseRefTarget);
            fail("Should throw an RestoreException");
        } catch (Exception e) {
            assertThat(e).isInstanceOf(RestoreException.class);
            assertThat(e.getCause()).isInstanceOf(RestoreException.class);
        }
    }

    @Test
    public void restore_goes_in_error_if_proccess_exit() throws CannotFindDatabaseDumperException {
        DatabaseDriver databaseDriver = new ErroredDatabaseDriver();
        when(dbDumpersFactory.getDatabaseDumper(databaseRef)).thenReturn(databaseDriver);
        when(databaseDumpFileRepo.findFirstByDatabaseRefOrderByCreatedAtDesc(databaseRef)).thenReturn(databaseDumpFile);
        try {
            this.coreRestorer.restore(databaseRef, databaseRef);
            fail("Should throw an RunProcessException");
        } catch (Exception e) {
            assertThat(e).isInstanceOf(RestoreException.class);
            assertThat(e.getCause()).isInstanceOf(RunProcessException.class);
        }

    }
}