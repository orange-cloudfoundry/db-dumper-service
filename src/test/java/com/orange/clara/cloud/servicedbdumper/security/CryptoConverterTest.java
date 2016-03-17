package com.orange.clara.cloud.servicedbdumper.security;

import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;

import static org.fest.assertions.Assertions.assertThat;

/**
 * Copyright (C) 2016 Arthur Halet
 * <p>
 * This software is distributed under the terms and conditions of the 'MIT'
 * license which can be found in the file 'LICENSE' in this package distribution
 * or at 'http://opensource.org/licenses/MIT'.
 * <p>
 * Author: Arthur Halet
 * Date: 17/03/2016
 */
public class CryptoConverterTest {

    private final static String notCryptedText = "mytext";
    private final static String cryptedText = "krLJzJJnug49TgUjFSvJHg==";
    CryptoConverter cryptoConverter = new CryptoConverter();

    @Before
    public void init() throws IOException, NoSuchAlgorithmException {
        cryptoConverter.encryptionKey = "supersecretttttt".getBytes();
    }

    @Test
    public void testConvertToDatabaseColumn() throws Exception {
        assertThat(cryptoConverter.convertToDatabaseColumn(notCryptedText)).isEqualTo(cryptedText);
    }

    @Test
    public void testConvertToEntityAttribute() throws Exception {
        assertThat(cryptoConverter.convertToEntityAttribute(cryptedText)).isEqualTo(notCryptedText);
    }
}