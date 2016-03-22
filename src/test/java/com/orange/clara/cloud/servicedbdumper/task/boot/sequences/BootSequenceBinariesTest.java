package com.orange.clara.cloud.servicedbdumper.task.boot.sequences;

import com.google.common.io.Files;
import com.orange.clara.cloud.servicedbdumper.exception.BootSequenceException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;

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
 * Date: 21/03/2016
 */
public class BootSequenceBinariesTest {
    BootSequenceBinaries bootSequence = new BootSequenceBinaries();
    File folder = new File(System.getProperty("java.io.tmpdir") + "/bootbinaries");
    File execFile = new File(folder.getAbsolutePath() + "/exec");

    @Before
    public void init() throws IOException {
        folder.mkdirs();
        execFile.createNewFile();
        Files.write("exit 2", execFile, Charset.defaultCharset());
        bootSequence.binariesPath = folder;
    }

    @Test
    public void when_executing_sequence_it_should_make_executable_the_file_inside_folder() throws BootSequenceException {
        String[] commands = {execFile.getAbsolutePath()};
        ProcessBuilder pb = new ProcessBuilder(commands);
        Process p = null;
        try {
            p = pb.start();
            p.waitFor();
            fail("File should not be executable");
        } catch (IOException | InterruptedException e) {

        }
        bootSequence.runSequence();
        pb = new ProcessBuilder(commands);
        p = null;
        try {
            p = pb.start();
            p.waitFor();
            assertThat(p.exitValue()).isEqualTo(2);
        } catch (IOException | InterruptedException e) {
            fail(e.getMessage(), e);
        }
    }

    @After
    public void clean() {
        execFile.delete();
        folder.delete();
    }
}