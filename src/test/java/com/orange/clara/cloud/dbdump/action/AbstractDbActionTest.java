package com.orange.clara.cloud.dbdump.action;

import com.orange.clara.cloud.model.DatabaseRef;
import org.junit.Test;

import java.io.IOException;

import static org.fest.assertions.Assertions.assertThat;


/**
 * Copyright (C) 2015 Orange
 * <p/>
 * This software is distributed under the terms and conditions of the 'MIT'
 * license which can be found in the file 'LICENSE' in this package distribution
 * or at 'http://opensource.org/licenses/MIT'.
 * <p/>
 * Author: Arthur Halet
 * Date: 14/09/2015
 */
public class AbstractDbActionTest {

    @Test
    public void runs_commandline_and_returns_output() throws Exception {
        //given
        //a commandline
        String [] commandLine = {"java", "-version"};

        //when
        AbstractDbAction cmdLineRunner = new AbstractDbAction() {
            @Override
            public String action(DatabaseRef databaseRef) throws IOException, InterruptedException {
                return null;
            }
        };

        //then
        //command output is available:
        String output = cmdLineRunner.runCommandLine(commandLine);

        assertThat(output).contains("version");
    }
}