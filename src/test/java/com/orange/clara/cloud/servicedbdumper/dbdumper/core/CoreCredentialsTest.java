package com.orange.clara.cloud.servicedbdumper.dbdumper.core;

import com.orange.clara.cloud.servicedbdumper.exception.CannotFindDatabaseDumperException;
import com.orange.clara.cloud.servicedbdumper.exception.DatabaseExtractionException;
import com.orange.clara.cloud.servicedbdumper.helper.UrlForge;
import com.orange.clara.cloud.servicedbdumper.model.*;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static org.fest.assertions.Assertions.assertThat;
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
public class CoreCredentialsTest extends AbstractCoreTester {
    private final static String fakeUrl = "http://test.com/";
    private final static DatabaseType DATABASE_TYPE = DatabaseType.MYSQL;
    private final static String DATABASE_NAME = "database";
    @InjectMocks
    CoreCredentials coreCredentials;
    @Mock
    UrlForge urlForge;

    @Before
    public void init() throws DatabaseExtractionException, CannotFindDatabaseDumperException {
        initMocks(this);
    }

    @Test
    public void get_credentials_give_the_right_list_of_db_dumper_credentials() throws DatabaseExtractionException {
        DatabaseRef databaseRef = this.generateDatabaseRef("mysql://user@mydb.com/" + DATABASE_NAME);
        DbDumperServiceInstance dbDumperServiceInstance = this.generateDbDumperServiceInstance(databaseRef);
        DatabaseDumpFile databaseDumpFile1 = this.generateDatabaseDumpFile(1, dbDumperServiceInstance);
        DatabaseDumpFile databaseDumpFile2 = this.generateDatabaseDumpFile(2, dbDumperServiceInstance);
        Date date = new Date();
        databaseDumpFile1.setCreatedAt(date);
        databaseDumpFile2.setCreatedAt(date);

        DbDumperCredential dbDumperCredential1 = new DbDumperCredential(1, date, fakeUrl + "1", fakeUrl + "1", databaseDumpFile1.getFileName(), databaseDumpFile1.getSize(), databaseDumpFile1.getDeleted(), DATABASE_TYPE, DATABASE_NAME);
        DbDumperCredential dbDumperCredential2 = new DbDumperCredential(2, date, fakeUrl + "2", fakeUrl + "2", databaseDumpFile2.getFileName(), databaseDumpFile2.getSize(), databaseDumpFile2.getDeleted(), DATABASE_TYPE, DATABASE_NAME);
        List<DbDumperCredential> expectedDumperCredentials = Arrays.asList(dbDumperCredential1, dbDumperCredential2);

        when(urlForge.createDownloadLink(databaseDumpFile1)).thenReturn(fakeUrl + databaseDumpFile1.getId());
        when(urlForge.createDownloadLink(databaseDumpFile2)).thenReturn(fakeUrl + databaseDumpFile2.getId());
        when(urlForge.createShowLink(databaseDumpFile1)).thenReturn(fakeUrl + databaseDumpFile1.getId());
        when(urlForge.createShowLink(databaseDumpFile2)).thenReturn(fakeUrl + databaseDumpFile2.getId());

        List<DbDumperCredential> dbDumperCredentials = this.coreCredentials.getDumpsCredentials(dbDumperServiceInstance);

        assertThat(dbDumperCredentials).hasSize(2);
        assertDbDumpersCredentialsReceive(expectedDumperCredentials, dbDumperCredentials);
    }

    @Test
    public void when_asking_to_have_all_dumps_for_a_database_get_credentials_give_the_right_list_of_db_dumper_credentials() throws DatabaseExtractionException {
        DatabaseRef databaseRef = this.generateDatabaseRef("mysql://user@mydb.com/" + DATABASE_NAME);
        DbDumperServiceInstance dbDumperServiceInstance1 = this.generateDbDumperServiceInstance(databaseRef);
        DbDumperServiceInstance dbDumperServiceInstance2 = this.generateDbDumperServiceInstance(databaseRef);

        DatabaseDumpFile databaseDumpFile1 = this.generateDatabaseDumpFile(1, dbDumperServiceInstance1);
        DatabaseDumpFile databaseDumpFile2 = this.generateDatabaseDumpFile(2, dbDumperServiceInstance1);
        DatabaseDumpFile databaseDumpFile3 = this.generateDatabaseDumpFile(3, dbDumperServiceInstance2);
        DatabaseDumpFile databaseDumpFile4 = this.generateDatabaseDumpFile(4, dbDumperServiceInstance2);

        Date date = new Date();
        databaseDumpFile1.setCreatedAt(date);
        databaseDumpFile2.setCreatedAt(date);
        databaseDumpFile3.setCreatedAt(date);
        databaseDumpFile4.setCreatedAt(date);

        DbDumperCredential dbDumperCredential1 = new DbDumperCredential(1, date, fakeUrl + "1", fakeUrl + "1", databaseDumpFile1.getFileName(), databaseDumpFile1.getSize(), databaseDumpFile1.getDeleted(), DATABASE_TYPE, DATABASE_NAME);
        DbDumperCredential dbDumperCredential2 = new DbDumperCredential(2, date, fakeUrl + "2", fakeUrl + "2", databaseDumpFile2.getFileName(), databaseDumpFile2.getSize(), databaseDumpFile2.getDeleted(), DATABASE_TYPE, DATABASE_NAME);
        DbDumperCredential dbDumperCredential3 = new DbDumperCredential(3, date, fakeUrl + "3", fakeUrl + "3", databaseDumpFile3.getFileName(), databaseDumpFile3.getSize(), databaseDumpFile3.getDeleted(), DATABASE_TYPE, DATABASE_NAME);
        DbDumperCredential dbDumperCredential4 = new DbDumperCredential(4, date, fakeUrl + "4", fakeUrl + "4", databaseDumpFile4.getFileName(), databaseDumpFile4.getSize(), databaseDumpFile4.getDeleted(), DATABASE_TYPE, DATABASE_NAME);

        List<DbDumperCredential> expectedDumperCredentials = Arrays.asList(dbDumperCredential1, dbDumperCredential2, dbDumperCredential3, dbDumperCredential4);

        when(urlForge.createDownloadLink(databaseDumpFile1)).thenReturn(fakeUrl + databaseDumpFile1.getId());
        when(urlForge.createDownloadLink(databaseDumpFile2)).thenReturn(fakeUrl + databaseDumpFile2.getId());
        when(urlForge.createDownloadLink(databaseDumpFile3)).thenReturn(fakeUrl + databaseDumpFile3.getId());
        when(urlForge.createDownloadLink(databaseDumpFile4)).thenReturn(fakeUrl + databaseDumpFile4.getId());

        when(urlForge.createShowLink(databaseDumpFile1)).thenReturn(fakeUrl + databaseDumpFile1.getId());
        when(urlForge.createShowLink(databaseDumpFile2)).thenReturn(fakeUrl + databaseDumpFile2.getId());
        when(urlForge.createShowLink(databaseDumpFile3)).thenReturn(fakeUrl + databaseDumpFile3.getId());
        when(urlForge.createShowLink(databaseDumpFile4)).thenReturn(fakeUrl + databaseDumpFile4.getId());

        List<DbDumperCredential> dbDumperCredentials = this.coreCredentials.getDumpsCredentials(databaseRef);

        assertThat(dbDumperCredentials).hasSize(4);
        assertDbDumpersCredentialsReceive(expectedDumperCredentials, dbDumperCredentials);
    }

    public void assertDbDumpersCredentialsReceive(List<DbDumperCredential> expectedDumperCredentials, List<DbDumperCredential> dbDumperCredentials) {
        for (DbDumperCredential dbDumperCredential : expectedDumperCredentials) {
            assertThat(dbDumperCredentials).contains(dbDumperCredential);

            DbDumperCredential actualDbDumperCredential = dbDumperCredentials.get(dbDumperCredentials.indexOf(dbDumperCredential));
            assertThat(actualDbDumperCredential.getCreatedAt()).isEqualTo(dbDumperCredential.getCreatedAt());
            assertThat(actualDbDumperCredential.getSize()).isEqualTo(dbDumperCredential.getSize());
            assertThat(actualDbDumperCredential.getDownloadUrl()).isEqualTo(dbDumperCredential.getDownloadUrl());
            assertThat(actualDbDumperCredential.getShowUrl()).isEqualTo(dbDumperCredential.getShowUrl());
            assertThat(actualDbDumperCredential.getDeleted()).isEqualTo(dbDumperCredential.getDeleted());
            assertThat(actualDbDumperCredential.getFilename()).isEqualTo(dbDumperCredential.getFilename());
            assertThat(actualDbDumperCredential.getTags()).isEqualTo(dbDumperCredential.getTags());
            assertThat(actualDbDumperCredential.getDatabaseType()).isEqualTo(dbDumperCredential.getDatabaseType());

        }

    }

}