package com.orange.clara.cloud.servicedbdumper.dbdumper.core;

import com.orange.clara.cloud.servicedbdumper.dbdumper.core.dbdrivers.DatabaseDriver;
import com.orange.clara.cloud.servicedbdumper.dbdumper.core.dbdrivers.DbDumpersFactory;
import com.orange.clara.cloud.servicedbdumper.exception.CannotFindDatabaseDumperException;
import com.orange.clara.cloud.servicedbdumper.exception.DatabaseExtractionException;
import com.orange.clara.cloud.servicedbdumper.exception.RestoreException;
import com.orange.clara.cloud.servicedbdumper.exception.RunProcessException;
import com.orange.clara.cloud.servicedbdumper.fake.databasedrivers.EchoDatabaseDriver;
import com.orange.clara.cloud.servicedbdumper.fake.databasedrivers.ErroredDatabaseDriver;
import com.orange.clara.cloud.servicedbdumper.fake.filer.EchoFiler;
import com.orange.clara.cloud.servicedbdumper.model.DatabaseDumpFile;
import com.orange.clara.cloud.servicedbdumper.model.DatabaseRef;
import com.orange.clara.cloud.servicedbdumper.model.DbDumperServiceInstance;
import com.orange.clara.cloud.servicedbdumper.repo.DatabaseDumpFileRepo;
import com.orange.clara.cloud.servicedbdumper.repo.DbDumperServiceInstanceRepo;
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
    private final static String textToEcho = "testit";
    @InjectMocks
    CoreRestorer coreRestorer;
    @Mock
    DbDumpersFactory dbDumpersFactory;
    @Mock
    DatabaseDumpFileRepo databaseDumpFileRepo;
    @Mock
    DbDumperServiceInstanceRepo serviceInstanceRepo;
    DatabaseRef databaseRef;
    DatabaseDumpFile databaseDumpFile;
    EchoFiler echoFiler;
    DbDumperServiceInstance dbDumperServiceInstance;

    @Before
    public void init() throws DatabaseExtractionException, CannotFindDatabaseDumperException {
        initMocks(this);
        databaseRef = this.generateDatabaseRef(DB_URI);
        dbDumperServiceInstance = this.generateDbDumperServiceInstance(databaseRef);
        databaseDumpFile = this.generateDatabaseDumpFile(dbDumperServiceInstance);
        DatabaseDriver databaseDriver = new EchoDatabaseDriver();

        databaseDriver.setDatabaseRef(databaseRef);
        echoFiler = new EchoFiler(textToEcho);
        this.coreRestorer.dateFormat = "dd-MM-yyyy HH:mm";
        this.coreRestorer.filer = echoFiler;
        when(dbDumpersFactory.getDatabaseDumper(databaseRef)).thenReturn(databaseDriver);

    }

    @Test
    public void ensure_no_error_is_thrown_when_restore_with_no_date() throws RestoreException {
        when(databaseDumpFileRepo.findFirstByDbDumperServiceInstanceOrderByCreatedAtDesc(dbDumperServiceInstance)).thenReturn(databaseDumpFile);

        this.coreRestorer.restore(dbDumperServiceInstance, databaseRef);
        assertThat(this.echoFiler.getLastTextInStream()).isEqualTo(textToEcho);
    }

    @Test
    public void ensure_no_error_is_thrown_when_restore_with_date() throws CannotFindDatabaseDumperException, RestoreException {
        Date date = new Date();
        when(databaseDumpFileRepo.findFirstByDbDumperServiceInstanceAndCreatedAtGreaterThanEqualOrderByCreatedAtAsc(dbDumperServiceInstance, date)).thenReturn(databaseDumpFile);
        this.coreRestorer.restore(dbDumperServiceInstance, databaseRef, date);
        assertThat(this.echoFiler.getLastTextInStream()).isEqualTo(textToEcho);
    }

    @Test
    public void restore_goes_in_error_if_databases_types_dont_match() throws CannotFindDatabaseDumperException, DatabaseExtractionException {
        DatabaseRef databaseRefTarget = this.generateDatabaseRef("postgres://mydb.com/database");
        when(databaseDumpFileRepo.findFirstByDbDumperServiceInstanceOrderByCreatedAtDesc(dbDumperServiceInstance)).thenReturn(databaseDumpFile);
        try {
            this.coreRestorer.restore(dbDumperServiceInstance, databaseRefTarget);
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
        when(databaseDumpFileRepo.findFirstByDbDumperServiceInstanceOrderByCreatedAtDesc(dbDumperServiceInstance)).thenReturn(databaseDumpFile);
        try {
            this.coreRestorer.restore(dbDumperServiceInstance, databaseRef);
            fail("Should throw an RunProcessException");
        } catch (Exception e) {
            assertThat(e).isInstanceOf(RestoreException.class);
            assertThat(e.getCause()).isInstanceOf(RunProcessException.class);
        }

    }
}