package com.orange.clara.cloud.servicedbdumper.filer.compression;

import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import static org.fest.assertions.Assertions.assertThat;

/**
 * Copyright (C) 2016 Arthur Halet
 * <p>
 * This software is distributed under the terms and conditions of the 'MIT'
 * license which can be found in the file 'LICENSE' in this package distribution
 * or at 'http://opensource.org/licenses/MIT'.
 * <p>
 * Author: Arthur Halet
 * Date: 14/03/2016
 */
public class GzipCompressingTest {

    private GzipCompressing gzipCompressing = new GzipCompressing();

    private byte[] stringUncompressedBytes = "this is a test".getBytes();
    private byte[] stringCompressedBytes = new byte[]{
            (byte) 0x1F, (byte) 0x8B, (byte) 0x08, (byte) 0x00, (byte) 0x00,
            (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
            (byte) 0x2B, (byte) 0xC9, (byte) 0xC8, (byte) 0x2C, (byte) 0x56,
            (byte) 0x00, (byte) 0xA2, (byte) 0x44, (byte) 0x85, (byte) 0x92,
            (byte) 0xD4, (byte) 0xE2, (byte) 0x12, (byte) 0x00, (byte) 0xEA,
            (byte) 0xE7, (byte) 0x1E, (byte) 0x0D, (byte) 0x0E, (byte) 0x00,
            (byte) 0x00, (byte) 0x00
    };

    @Test
    public void testGunziptIt() throws Exception {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        this.gzipCompressing.gunziptIt(outputStream, new ByteArrayInputStream(this.stringCompressedBytes));
        assertThat(outputStream.toByteArray()).isEqualTo(this.stringUncompressedBytes);
    }

    @Test
    public void testGziptIt() throws Exception {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        this.gzipCompressing.gziptIt(new ByteArrayInputStream(stringUncompressedBytes), outputStream);
        assertThat(outputStream.toByteArray()).isEqualTo(this.stringCompressedBytes);
    }
}