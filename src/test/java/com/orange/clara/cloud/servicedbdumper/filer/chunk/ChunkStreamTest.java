package com.orange.clara.cloud.servicedbdumper.filer.chunk;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Spy;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import static org.mockito.Matchers.notNull;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.MockitoAnnotations.initMocks;

/**
 * Copyright (C) 2016 Arthur Halet
 * <p>
 * This software is distributed under the terms and conditions of the 'MIT'
 * license which can be found in the file 'LICENSE' in this package distribution
 * or at 'http://opensource.org/licenses/MIT'.
 * <p>
 * Author: Arthur Halet
 * Date: 21/09/2016
 */
public class ChunkStreamTest {
    @Spy
    private ChunkStream chunkStream = new ChunkStream();

    private byte[] stringBytes = "this is a test".getBytes();

    @Before
    public void setup() {
        initMocks(this);
        chunkStream.setChunkSize(1);
    }

    @Test
    public void chunkItTest() throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        this.chunkStream.chunkIt(outputStream, new ByteArrayInputStream(this.stringBytes));
        verify(chunkStream, times(stringBytes.length + 1)).readInChunk((InputStream) notNull(), (byte[]) notNull());
    }
}