package com.orange.clara.cloud.servicedbdumper.helper;

import org.junit.Test;

import static org.fest.assertions.Assertions.assertThat;

/**
 * Copyright (C) 2016 Arthur Halet
 * <p>
 * This software is distributed under the terms and conditions of the 'MIT'
 * license which can be found in the file 'LICENSE' in this package distribution
 * or at 'http://opensource.org/licenses/MIT'.
 * <p>
 * Author: Arthur Halet
 * Date: 16/03/2016
 */
public class ByteFormatTest {


    @Test
    public void testParse() throws Exception {
        assertThat(ByteFormat.parse("12")).isEqualTo(12);
        assertThat(ByteFormat.parse("1KB")).isEqualTo(1 * 1024);
        assertThat(ByteFormat.parse("1ko")).isEqualTo(1 * 1024);
        assertThat(ByteFormat.parse("1mo")).isEqualTo(1 * 1024 * 1024);
        assertThat(ByteFormat.parse("1g")).isEqualTo(1 * 1024 * 1024 * 1024);
        assertThat(ByteFormat.parse("1to")).isEqualTo(1 * 1024 * 1024 * 1024 * 1024);
    }
}