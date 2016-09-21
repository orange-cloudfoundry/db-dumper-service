package com.orange.clara.cloud.servicedbdumper.filer;

import com.orange.clara.cloud.servicedbdumper.filer.chunk.ChunkStream;

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
public class ChunkFilerTest extends GzipDiskFilerTest {
    public ChunkFilerTest() {
        super();
        ChunkStream chunkStream = new ChunkStream();
        chunkStream.setChunkSize(1);
        this.filer = new ChunkFiler(this.filer, chunkStream);
    }
}