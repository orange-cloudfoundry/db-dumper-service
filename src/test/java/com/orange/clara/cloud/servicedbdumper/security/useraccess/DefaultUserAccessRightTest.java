package com.orange.clara.cloud.servicedbdumper.security.useraccess;


import com.orange.clara.cloud.servicedbdumper.exception.UserAccessRightException;
import com.orange.clara.cloud.servicedbdumper.model.DatabaseRef;
import com.orange.clara.cloud.servicedbdumper.model.DbDumperServiceInstance;
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
 * Date: 18/03/2016
 */
public class DefaultUserAccessRightTest {

    DefaultUserAccessRight defaultUserAccessRight = new DefaultUserAccessRight();

    @Test
    public void when_check_access_it_should_always_give_access() throws UserAccessRightException {
        assertThat(defaultUserAccessRight.haveAccessToServiceInstance((DatabaseRef) null)).isTrue();
        assertThat(defaultUserAccessRight.haveAccessToServiceInstance((DbDumperServiceInstance) null)).isTrue();
        assertThat(defaultUserAccessRight.haveAccessToServiceInstance("")).isTrue();
    }

}