package com.orange.clara.cloud.servicedbdumper.task.boot;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

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
@Component
public class Boot {

    @Value("${encryption.key:MySuperSecretKey}")
    private String encryptionKey;

    @Value("classpath:properties/encryption_key.txt")
    private File encryptionKeyFile;

    public void fillEncryptionKeyFile() throws IOException, NoSuchAlgorithmException {
        //force to have always a 32 bytes key (to use AES encryption with 256 bits key length)
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        md.update(encryptionKey.getBytes(Charsets.UTF_8));
        byte[] key = md.digest();

        Files.write(key, encryptionKeyFile);
    }

    @PostConstruct
    public void boot() {
        try {
            this.fillEncryptionKeyFile();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
