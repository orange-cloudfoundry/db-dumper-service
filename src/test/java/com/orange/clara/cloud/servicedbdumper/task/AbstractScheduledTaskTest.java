package com.orange.clara.cloud.servicedbdumper.task;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.transaction.annotation.Transactional;

import static org.fest.assertions.Assertions.assertThat;

/**
 * Copyright (C) 2016 Orange
 * <p>
 * This software is distributed under the terms and conditions of the 'Apache-2.0'
 * license which can be found in the file 'LICENSE' in this package distribution
 * or at 'https://opensource.org/licenses/Apache-2.0'.
 * <p>
 * Author: Arthur Halet
 * Date: 21/03/2016
 */
abstract public class AbstractScheduledTaskTest {

    public void assertMethodsHaveScheduledAnnotations(Class clazz, String... methodNames) throws NoSuchMethodException {
        for (String methodName : methodNames) {
            assertThat(clazz.getMethod(methodName).isAnnotationPresent(Scheduled.class)).isTrue();
        }
    }

    public void assertMethodsHaveTransactionalAnnotations(Class clazz, String... methodNames) throws NoSuchMethodException {
        for (String methodName : methodNames) {
            assertThat(clazz.getMethod(methodName).isAnnotationPresent(Transactional.class)).isTrue();
        }
    }
}
