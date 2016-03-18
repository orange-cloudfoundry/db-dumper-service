package com.orange.clara.cloud.servicedbdumper.service.servicekey;

import com.orange.clara.cloud.servicedbdumper.exception.ServiceKeyException;
import com.orange.clara.cloud.servicedbdumper.model.DatabaseService;
import org.junit.Test;

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
 * Date: 18/03/2016
 */
public class NoServiceKeyManagerTest {
    NoServiceKeyManager noServiceKeyManager = new NoServiceKeyManager();

    @Test
    public void when_trying_creating_a_service_key_it_should_always_raise_an_exception() {
        try {
            this.noServiceKeyManager.createServiceKey("", "", "", "");
            fail("Should throw an ServiceKeyException");
        } catch (Exception e) {
            assertThat(e).isInstanceOf(ServiceKeyException.class);
        }

        try {
            this.noServiceKeyManager.createServiceKey((DatabaseService) null);
            fail("Should throw an ServiceKeyException");
        } catch (Exception e) {
            assertThat(e).isInstanceOf(ServiceKeyException.class);
        }
    }
}