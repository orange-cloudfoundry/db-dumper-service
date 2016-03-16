package com.orange.clara.cloud.servicedbdumper.filer;

import com.google.common.io.ByteStreams;
import com.google.common.io.Files;
import org.junit.After;
import org.junit.Test;

import java.io.*;

import static org.fest.assertions.Assertions.assertThat;

/**
 * Copyright (C) 2016 Orange
 * <p>
 * This software is distributed under the terms and conditions of the 'Apache-2.0'
 * license which can be found in the file 'LICENSE' in this package distribution
 * or at 'https://opensource.org/licenses/Apache-2.0'.
 * <p>
 * Author: Arthur Halet
 * Date: 16/03/2016
 */
abstract public class AbstractFilerTest {
    public String tmpFolder;
    protected String filename;
    protected byte[] fakeContent = "this is a test".getBytes();
    protected byte[] expectedContent = "this is a test".getBytes();
    protected byte[] rawContent = "this is a test".getBytes();
    protected Filer filer;

    public AbstractFilerTest(String tmpFolder, String filename, Filer filer) {
        this.tmpFolder = tmpFolder;
        this.filename = filename;
        this.filer = filer;
    }

    public void toCompressedRawContent() {
        this.rawContent = new byte[]{
                (byte) 0x1F, (byte) 0x8B, (byte) 0x08, (byte) 0x00, (byte) 0x00,
                (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
                (byte) 0x2B, (byte) 0xC9, (byte) 0xC8, (byte) 0x2C, (byte) 0x56,
                (byte) 0x00, (byte) 0xA2, (byte) 0x44, (byte) 0x85, (byte) 0x92,
                (byte) 0xD4, (byte) 0xE2, (byte) 0x12, (byte) 0x00, (byte) 0xEA,
                (byte) 0xE7, (byte) 0x1E, (byte) 0x0D, (byte) 0x0E, (byte) 0x00,
                (byte) 0x00, (byte) 0x00
        };
    }

    protected File getFile(String filename) {
        return new File(tmpFolder + "/" + filename);
    }

    @Test
    public void when_store_a_file_it_should_be_available_on_disk_and_have_the_good_content() throws IOException {
        filer.store(new ByteArrayInputStream(fakeContent), filename);
        File file = this.getFile(filename);
        assertThat(file).isNotNull();
        assertThat(file.isFile()).isTrue();

        byte[] content = Files.toByteArray(file);
        assertThat(content).isEqualTo(rawContent);
    }

    @Test
    public void when_retrieve_a_file_it_should_give_the_good_content() throws IOException {
        filer.store(new ByteArrayInputStream(fakeContent), filename);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        filer.retrieve(outputStream, filename);
        assertThat(outputStream.toByteArray()).isEqualTo(expectedContent);
    }

    @Test
    public void when_retrieve_with_stream_a_file_it_should_give_the_good_content() throws IOException {
        filer.store(new ByteArrayInputStream(fakeContent), filename);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        InputStream dumpFileInputStream = filer.retrieveWithStream(filename);
        assertThat(dumpFileInputStream).isNotNull();

        ByteStreams.copy(dumpFileInputStream, outputStream);
        outputStream.flush();
        dumpFileInputStream.close();
        outputStream.close();
        assertThat(outputStream.toByteArray()).isEqualTo(expectedContent);
    }

    @Test
    public void when_retrieve_with_original_stream_a_file_it_should_give_the_good_content() throws IOException {
        filer.store(new ByteArrayInputStream(fakeContent), filename);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        InputStream dumpFileInputStream = filer.retrieveWithOriginalStream(filename);
        assertThat(dumpFileInputStream).isNotNull();

        ByteStreams.copy(dumpFileInputStream, outputStream);
        outputStream.flush();
        dumpFileInputStream.close();
        outputStream.close();
        assertThat(outputStream.toByteArray()).isEqualTo(rawContent);
    }

    @Test
    public void when_delete_file_it_should_not_be_available_on_disk() throws IOException {
        filer.store(new ByteArrayInputStream(fakeContent), filename);
        File file = this.getFile(filename);
        assertThat(file).isNotNull();
        assertThat(file.isFile()).isTrue();
        filer.delete(filename);
        assertThat(file.isFile()).isFalse();
    }

    @Test
    public void when_getting_the_content_lenght_of_the_file_it_should_the_expected_one() throws IOException {
        filer.store(new ByteArrayInputStream(fakeContent), filename);
        long actualLenght = filer.getContentLength(filename);
        assertThat(actualLenght).isEqualTo(rawContent.length);
    }

    @After
    public void clean() {
        File file = new File(filename);
        if (!file.isFile()) {
            return;
        }
        file.delete();
        if (file.getParentFile().isDirectory()) {
            file.getParentFile().delete();
        }
    }
}
