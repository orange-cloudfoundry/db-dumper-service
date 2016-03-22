package com.orange.clara.cloud.servicedbdumper.task.boot.sequences;

import com.google.common.io.Files;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

import static org.fest.assertions.Assertions.assertThat;

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
public class BootSequenceSecurityTest {

    private final static String key = "key";
    private final static byte[] cryptedKey = new byte[]{
            (byte) 0x2C, (byte) 0x70, (byte) 0xE1, (byte) 0x2B, (byte) 0x7A,
            (byte) 0x06, (byte) 0x46, (byte) 0xF9, (byte) 0x22, (byte) 0x79,
            (byte) 0xF4, (byte) 0x27, (byte) 0xC7, (byte) 0xB3, (byte) 0x8E,
            (byte) 0x73, (byte) 0x34, (byte) 0xD8, (byte) 0xE5, (byte) 0x38,
            (byte) 0x9C, (byte) 0xFF, (byte) 0x16, (byte) 0x7A, (byte) 0x1D,
            (byte) 0xC3, (byte) 0x0E, (byte) 0x73, (byte) 0xF8, (byte) 0x26,
            (byte) 0xB6, (byte) 0x83
    };
    BootSequenceSecurity bootSequence = new BootSequenceSecurity();
    File encryptionFile = new File(System.getProperty("java.io.tmpdir") + "/properties/encryption_key.txt");

    @Before
    public void init() throws IOException {
        encryptionFile.getParentFile().mkdirs();
        encryptionFile.createNewFile();
        bootSequence.encryptionKeyFile = encryptionFile;
        bootSequence.encryptionKey = key;
    }

    @Test
    public void when_running_sequence_it_should_create_an_encrypted_key_in_file() throws IOException {
        bootSequence.runSequence();
        byte[] cryptedResult = Files.toByteArray(encryptionFile);
        assertThat(cryptedResult.length).isEqualTo(256 / 8);
        assertThat(Files.toByteArray(encryptionFile)).isEqualTo(cryptedKey);
    }

    @After
    public void clean() {
        encryptionFile.delete();
        encryptionFile.getParentFile().delete();
    }
}