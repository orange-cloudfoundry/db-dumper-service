package com.orange.clara.cloud.servicedbdumper.security;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.security.Key;
import java.util.Base64;

/**
 * Copyright (C) 2015 Orange
 * <p/>
 * This software is distributed under the terms and conditions of the 'MIT'
 * license which can be found in the file 'LICENSE' in this package distribution
 * or at 'http://opensource.org/licenses/MIT'.
 * <p/>
 * Author: Arthur Halet
 * Date: 08/12/2015
 */
@Converter
public class CryptoConverter implements AttributeConverter<String, String> {
    private static final String ALGORITHM = "AES/ECB/PKCS5Padding";
    private final static String ENCRYPTION_KEY_FILEPATH = "properties/encryption_key.txt";
    private byte[] encryptionKey;


    @Override
    public String convertToDatabaseColumn(String password) {
        try {
            this.loadKey();
            Key key = new SecretKeySpec(encryptionKey, "AES");
            Cipher c = Cipher.getInstance(ALGORITHM);
            c.init(Cipher.ENCRYPT_MODE, key);
            return Base64.getEncoder().encodeToString(c.doFinal(password.getBytes()));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String convertToEntityAttribute(String cryptedPassword) {
        try {
            this.loadKey();
            Key key = new SecretKeySpec(encryptionKey, "AES");
            Cipher c = Cipher.getInstance(ALGORITHM);
            c.init(Cipher.DECRYPT_MODE, key);
            return new String(c.doFinal(Base64.getDecoder().decode(cryptedPassword)));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void loadKey() throws URISyntaxException, IOException {
        ClassLoader classLoader = getClass().getClassLoader();
        File encryptionKeyFile = new File(classLoader.getResource(ENCRYPTION_KEY_FILEPATH).toURI());
        this.encryptionKey = Files.readAllBytes(encryptionKeyFile.toPath());
    }
}