package com.orange.clara.cloud.servicedbdumper.controllers;

import com.orange.clara.cloud.servicedbdumper.Application;
import com.orange.clara.cloud.servicedbdumper.config.Routes;
import com.orange.clara.cloud.servicedbdumper.dbdumper.Deleter;
import com.orange.clara.cloud.servicedbdumper.exception.DumpFileShowException;
import com.orange.clara.cloud.servicedbdumper.fake.configuration.DbDumperConfigContextMock;
import com.orange.clara.cloud.servicedbdumper.fake.configuration.FilerConfigContext;
import com.orange.clara.cloud.servicedbdumper.fake.dbdumper.mocked.DeleterMock;
import com.orange.clara.cloud.servicedbdumper.model.DatabaseDumpFile;
import com.orange.clara.cloud.servicedbdumper.model.DatabaseRef;
import com.orange.clara.cloud.servicedbdumper.repo.DatabaseDumpFileRepo;
import com.orange.clara.cloud.servicedbdumper.repo.DatabaseRefRepo;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.util.NestedServletException;

import java.net.URI;
import java.util.Base64;

import static org.fest.assertions.Assertions.assertThat;
import static org.fest.assertions.Fail.fail;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

/**
 * Copyright (C) 2016 Arthur Halet
 * <p>
 * This software is distributed under the terms and conditions of the 'MIT'
 * license which can be found in the file 'LICENSE' in this package distribution
 * or at 'http://opensource.org/licenses/MIT'.
 * <p>
 * Author: Arthur Halet
 * Date: 24/03/2016
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = {Application.class, FilerConfigContext.class, DbDumperConfigContextMock.class})
@WebAppConfiguration
@ActiveProfiles("local")
public class ManagerControllerTest {
    private final static String fileNameShowable = "file-1";
    private final static String databaseNameShowable = "database-1";
    private final static String userShowable = "user-1";
    private final static String passwordShowable = "password-1";
    private final static long sizeShowable = 12L;
    private final static String fileNameNotShowable = "file-2";
    private final static String databaseNameNotShowable = "database-2";
    private final static String userNotShowable = "user-2";
    private final static String passwordNotShowable = "password-2";
    private final static long sizeNotShowable = 24L;
    private static Integer databaseDumpFileShowableId;
    private static Integer databaseDumpFileNotShowableId;
    private static Integer databaseDumpFileWithDeletedDbId;
    private static Integer databaseDumpFileDeletedId;
    private DatabaseDumpFile databaseDumpFileShowable;
    private DatabaseDumpFile databaseDumpFileNotShowable;
    private DatabaseDumpFile databaseDumpFileWithDeletedDb;
    private DatabaseDumpFile databaseDumpFileDeleted;
    @Autowired
    private Deleter deleter;
    @Autowired
    private DatabaseDumpFileRepo databaseDumpFileRepo;
    private MockMvc mockMvc;
    @Autowired
    private WebApplicationContext webApplicationContext;
    @Autowired
    private DatabaseRefRepo databaseRefRepo;
    @Autowired
    @Qualifier("testTextForFiler")
    private String textForFiler;


    @Before
    public void setup() throws Exception {
        this.mockMvc = webAppContextSetup(webApplicationContext).build();
        DatabaseRef databaseRef;
        if (!databaseRefRepo.exists(databaseNameNotShowable)) {
            databaseRef = new DatabaseRef(databaseNameShowable, URI.create("mysql://foo:bar@mymysql-1/mydb"));
            databaseRefRepo.save(databaseRef);
        } else {
            databaseRef = databaseRefRepo.findOne(databaseNameShowable);
        }
        DatabaseRef databaseRefDeleted;
        if (!databaseRefRepo.exists(databaseNameNotShowable)) {
            databaseRefDeleted = new DatabaseRef(databaseNameNotShowable, URI.create("mysql://foo:bar@mymysql-2/mydb"));
            databaseRefDeleted.setDeleted(true);
            databaseRefRepo.save(databaseRefDeleted);
        } else {
            databaseRefDeleted = databaseRefRepo.findOne(databaseNameNotShowable);
        }
        if (databaseDumpFileShowableId != null && databaseDumpFileRepo.exists(databaseDumpFileShowableId)) {
            databaseDumpFileShowable = databaseDumpFileRepo.findOne(databaseDumpFileShowableId);
        } else {
            databaseDumpFileShowable = new DatabaseDumpFile(fileNameShowable, databaseRef, userShowable, passwordShowable, true, sizeShowable);
            databaseDumpFileRepo.save(databaseDumpFileShowable);
        }

        if (databaseDumpFileNotShowableId != null && databaseDumpFileRepo.exists(databaseDumpFileNotShowableId)) {
            databaseDumpFileNotShowable = databaseDumpFileRepo.findOne(databaseDumpFileNotShowableId);
        } else {
            databaseDumpFileNotShowable = new DatabaseDumpFile(fileNameNotShowable, databaseRef, userNotShowable, passwordNotShowable, false, sizeNotShowable);
            databaseDumpFileRepo.save(databaseDumpFileNotShowable);
        }
        if (databaseDumpFileDeletedId != null && databaseDumpFileRepo.exists(databaseDumpFileDeletedId)) {
            databaseDumpFileDeleted = databaseDumpFileRepo.findOne(databaseDumpFileDeletedId);
        } else {
            databaseDumpFileDeleted = new DatabaseDumpFile(fileNameNotShowable, databaseRef, userNotShowable, passwordNotShowable, false, sizeNotShowable);
            databaseDumpFileDeleted.setDeleted(true);
            databaseDumpFileRepo.save(databaseDumpFileDeleted);
        }
        if (databaseDumpFileWithDeletedDbId != null && databaseDumpFileRepo.exists(databaseDumpFileWithDeletedDbId)) {
            databaseDumpFileWithDeletedDb = databaseDumpFileRepo.findOne(databaseDumpFileWithDeletedDbId);
        } else {
            databaseDumpFileWithDeletedDb = new DatabaseDumpFile(fileNameNotShowable, databaseRefDeleted, userNotShowable, passwordNotShowable, false, sizeNotShowable);
            databaseDumpFileRepo.save(databaseDumpFileWithDeletedDb);
        }

    }


    @Test
    public void when_user_wants_to_download_dump_file_and_user_password_are_incorrect_it_should_reject_the_download() throws Exception {
        mockMvc.perform(get(Routes.MANAGE_ROOT + Routes.DOWNLOAD_DUMP_FILE_ROOT + "/" + databaseDumpFileShowable.getId()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void when_user_wants_to_download_dump_file_and_user_password_are_correct_it_should_download_the_content() throws Exception {

        //format to basic auth header
        String login = userShowable + ":" + passwordShowable;
        String loginEncoded = Base64.getEncoder().encodeToString(login.getBytes());

        mockMvc.perform(get(Routes.MANAGE_ROOT + Routes.DOWNLOAD_DUMP_FILE_ROOT + "/" + databaseDumpFileShowable.getId()).header("Authorization", "Basic " + loginEncoded))
                .andExpect(status().isOk())
                .andExpect(content().string(textForFiler));
    }

    @Test
    public void when_user_want_to_see_dump_file_in_raw_and_dump_file_is_showable_it_should_return_dump_file_in_raw() throws Exception {

        mockMvc.perform(get(Routes.MANAGE_ROOT + Routes.RAW_DUMP_FILE_ROOT + "/" + databaseDumpFileShowable.getId()))
                .andExpect(content().string(textForFiler + "\n"));
    }

    @Test
    public void when_user_want_to_see_dump_file_in_webpage_and_dump_file_is_showable_it_should_return_sql_and_filename_databasename_and_id() throws Exception {

        mockMvc.perform(get(Routes.MANAGE_ROOT + Routes.SHOW_DUMP_FILE_ROOT + "/" + databaseDumpFileShowable.getId()))
                .andExpect(model().attribute("databaseName", is(databaseNameShowable)))
                .andExpect(model().attribute("fileName", is(fileNameShowable)))
                .andExpect(model().attribute("id", is(databaseDumpFileShowable.getId())))
                .andExpect(model().attribute("sql", is(textForFiler + "\n")));
    }

    @Test
    public void when_user_want_to_delete_a_dump_file_it_should_call_deleter_to_delete_it_only_if_dump_file_is_not_deleted() throws Exception {
        DeleterMock deleterMock = (DeleterMock) this.deleter;
        deleterMock.resetNumberCallDelete();
        mockMvc.perform(get(Routes.MANAGE_ROOT + Routes.DELETE_DUMP_FILE_ROOT + "/" + databaseDumpFileShowable.getId()))
                .andExpect(redirectedUrl(String.format(Routes.MANAGE_ROOT + Routes.MANAGE_LIST_DATABASE_ROOT + "/%s", databaseDumpFileShowable.getDatabaseRef().getName())));

        mockMvc.perform(get(Routes.MANAGE_ROOT + Routes.DELETE_DUMP_FILE_ROOT + "/" + databaseDumpFileNotShowable.getId()))
                .andExpect(redirectedUrl(String.format(Routes.MANAGE_ROOT + Routes.MANAGE_LIST_DATABASE_ROOT + "/%s", databaseDumpFileNotShowable.getDatabaseRef().getName())));

        mockMvc.perform(get(Routes.MANAGE_ROOT + Routes.DELETE_DUMP_FILE_ROOT + "/" + databaseDumpFileDeleted.getId()))
                .andExpect(redirectedUrl(String.format(Routes.MANAGE_ROOT + Routes.MANAGE_LIST_DATABASE_ROOT + "/%s", databaseDumpFileDeleted.getDatabaseRef().getName())));
        assertThat(deleterMock.getNumberCallDelete()).isEqualTo(2);
    }

    @Test
    public void when_user_want_to_see_dump_file_and_dump_file_is_not_showable_or_deleted_it_should_throw_an_exception() throws Exception {
        try {
            mockMvc.perform(get(Routes.MANAGE_ROOT + Routes.RAW_DUMP_FILE_ROOT + "/" + databaseDumpFileNotShowable.getId()));
            fail("It should throw an DumpFileShowException");
        } catch (NestedServletException e) {
            assertThat(e.getCause()).isInstanceOf(DumpFileShowException.class);
        }
        try {
            mockMvc.perform(get(Routes.MANAGE_ROOT + Routes.SHOW_DUMP_FILE_ROOT + "/" + databaseDumpFileNotShowable.getId()));
            fail("It should throw an DumpFileShowException");
        } catch (NestedServletException e) {
            assertThat(e.getCause()).isInstanceOf(DumpFileShowException.class);
        }
        try {
            mockMvc.perform(get(Routes.MANAGE_ROOT + Routes.RAW_DUMP_FILE_ROOT + "/" + databaseDumpFileDeleted.getId()));
            fail("It should throw an DumpFileShowException");
        } catch (NestedServletException e) {
            assertThat(e.getCause()).isInstanceOf(DumpFileShowException.class);
        }
        try {
            mockMvc.perform(get(Routes.MANAGE_ROOT + Routes.SHOW_DUMP_FILE_ROOT + "/" + databaseDumpFileDeleted.getId()));
            fail("It should throw an DumpFileShowException");
        } catch (NestedServletException e) {
            assertThat(e.getCause()).isInstanceOf(DumpFileShowException.class);
        }
    }

    @Test
    public void when_user_want_to_see_or_download_or_delete_a_dump_file_which_not_exist_it_should_always_throw_an_exception() throws Exception {
        try {
            mockMvc.perform(get(Routes.MANAGE_ROOT + Routes.RAW_DUMP_FILE_ROOT + "/100"));
            fail("It should throw an IllegalArgumentException");
        } catch (NestedServletException e) {
            assertThat(e.getCause()).isInstanceOf(IllegalArgumentException.class);
        }
        try {
            mockMvc.perform(get(Routes.MANAGE_ROOT + Routes.SHOW_DUMP_FILE_ROOT + "/100"));
            fail("It should throw an IllegalArgumentException");
        } catch (NestedServletException e) {
            assertThat(e.getCause()).isInstanceOf(IllegalArgumentException.class);
        }

        try {
            mockMvc.perform(get(Routes.MANAGE_ROOT + Routes.DELETE_DUMP_FILE_ROOT + "/100"));
            fail("It should throw an IllegalArgumentException");
        } catch (NestedServletException e) {
            assertThat(e.getCause()).isInstanceOf(IllegalArgumentException.class);
        }

        try {
            mockMvc.perform(get(Routes.MANAGE_ROOT + Routes.DOWNLOAD_DUMP_FILE_ROOT + "/100"));
            fail("It should throw an IllegalArgumentException");
        } catch (NestedServletException e) {
            assertThat(e.getCause()).isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Test
    public void when_user_want_to_see_or_download_or_delete_a_dump_file_which_has_a_database_deleted_should_always_throw_an_exception() throws Exception {
        try {
            mockMvc.perform(get(Routes.MANAGE_ROOT + Routes.RAW_DUMP_FILE_ROOT + "/" + databaseDumpFileWithDeletedDb.getId()));
            fail("It should throw an IllegalArgumentException");
        } catch (NestedServletException e) {
            assertThat(e.getCause()).isInstanceOf(IllegalArgumentException.class);
        }
        try {
            mockMvc.perform(get(Routes.MANAGE_ROOT + Routes.SHOW_DUMP_FILE_ROOT + "/" + databaseDumpFileWithDeletedDb.getId()));
            fail("It should throw an IllegalArgumentException");
        } catch (NestedServletException e) {
            assertThat(e.getCause()).isInstanceOf(IllegalArgumentException.class);
        }

        try {
            mockMvc.perform(get(Routes.MANAGE_ROOT + Routes.DELETE_DUMP_FILE_ROOT + "/" + databaseDumpFileWithDeletedDb.getId()));
            fail("It should throw an IllegalArgumentException");
        } catch (NestedServletException e) {
            assertThat(e.getCause()).isInstanceOf(IllegalArgumentException.class);
        }

        try {
            mockMvc.perform(get(Routes.MANAGE_ROOT + Routes.DOWNLOAD_DUMP_FILE_ROOT + "/" + databaseDumpFileWithDeletedDb.getId()));
            fail("It should throw an IllegalArgumentException");
        } catch (NestedServletException e) {
            assertThat(e.getCause()).isInstanceOf(IllegalArgumentException.class);
        }
    }
}