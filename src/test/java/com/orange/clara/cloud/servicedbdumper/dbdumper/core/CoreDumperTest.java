package com.orange.clara.cloud.servicedbdumper.dbdumper.core;

import com.orange.clara.cloud.servicedbdumper.dbdumper.core.dbdrivers.DatabaseDriver;
import com.orange.clara.cloud.servicedbdumper.dbdumper.core.dbdrivers.DbDumpersFactory;
import com.orange.clara.cloud.servicedbdumper.dbdumper.fake.databasedrivers.EchoDatabaseDriver;
import com.orange.clara.cloud.servicedbdumper.dbdumper.fake.databasedrivers.ErroredDatabaseDriver;
import com.orange.clara.cloud.servicedbdumper.dbdumper.fake.filer.EchoFiler;
import com.orange.clara.cloud.servicedbdumper.exception.CannotFindDatabaseDumperException;
import com.orange.clara.cloud.servicedbdumper.exception.DatabaseExtractionException;
import com.orange.clara.cloud.servicedbdumper.exception.DumpException;
import com.orange.clara.cloud.servicedbdumper.exception.RunProcessException;
import com.orange.clara.cloud.servicedbdumper.model.DatabaseDumpFile;
import com.orange.clara.cloud.servicedbdumper.model.DatabaseRef;
import com.orange.clara.cloud.servicedbdumper.repo.DatabaseDumpFileRepo;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import static org.fest.assertions.Assertions.assertThat;
import static org.fest.assertions.Fail.fail;
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
public class CoreDumperTest extends AbstractCoreTester {


    public static final String DB_URI = "mysql://mydb.com/database";
    @InjectMocks
    CoreDumper coreDumper;


    @Mock
    DbDumpersFactory dbDumpersFactory;


    @Mock
    DatabaseDumpFileRepo databaseDumpFileRepo;

    DatabaseRef databaseRef;

    DatabaseDriver databaseDriver;

    EchoFiler filer;

    @Before
    public void init() throws DatabaseExtractionException, CannotFindDatabaseDumperException {
        initMocks(this);
        databaseRef = this.generateDatabaseRef(DB_URI);
        assertThat(databaseRef.getDatabaseDumpFiles()).isEmpty();
        databaseDriver = new EchoDatabaseDriver();

        databaseDriver.setDatabaseRef(databaseRef);

        when(dbDumpersFactory.getDatabaseDumper(databaseRef)).thenReturn(databaseDriver);
        when(databaseDumpFileRepo.save((DatabaseDumpFile) notNull())).thenReturn(null);

        filer = new EchoFiler("testit");
        this.coreDumper.dateFormat = "dd-MM-yyyy HH:mm";
        this.coreDumper.filer = filer;
    }

    @Test
    public void create_dump_to_database_associate_a_dump_file_to_it() throws DumpException {


        String expectedFileName = this.coreDumper.generateFileName(databaseDriver);
        long expectedSize = filer.getContentLength(null);
        String expectedStringInFiler = databaseRef.toString();
        this.coreDumper.dump(databaseRef);
        assertThat(databaseRef.getDatabaseDumpFiles()).hasSize(1);

        DatabaseDumpFile databaseDumpFile = databaseRef.getDatabaseDumpFiles().get(0);
        assertThat(databaseDumpFile.getCreatedAt()).isNotNull();
        assertThat(databaseDumpFile.getPassword()).isNotNull();
        assertThat(databaseDumpFile.getUser()).isNotNull();
        assertThat(databaseDumpFile.getShowable()).isTrue();
        assertThat(databaseDumpFile.getDeleted()).isFalse();
        assertThat(databaseDumpFile.getFileName()).isEqualTo(expectedFileName);
        assertThat(databaseDumpFile.getDeletedAt()).isNull();
        assertThat(databaseDumpFile.getSize()).isEqualTo(expectedSize);
        assertThat(filer.getLastTextInStream().trim()).isEqualTo(expectedStringInFiler);
    }

    @Test
    public void create_dump_goes_in_error_if_proccess_exit() throws CannotFindDatabaseDumperException {
        databaseDriver = new ErroredDatabaseDriver();

        when(dbDumpersFactory.getDatabaseDumper(databaseRef)).thenReturn(databaseDriver);
        try {
            this.coreDumper.dump(databaseRef);
            fail("Should throw an RunProcessException");
        } catch (Exception e) {
            assertThat(e).isInstanceOf(DumpException.class);
            assertThat(e.getCause()).isInstanceOf(RunProcessException.class);
        }

    }
}