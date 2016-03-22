package com.orange.clara.cloud.servicedbdumper.dbdumper.fake.services;

import com.orange.clara.cloud.servicedbdumper.security.AccessManager;

/**
 * Copyright (C) 2016 Orange
 * <p>
 * This software is distributed under the terms and conditions of the 'Apache-2.0'
 * license which can be found in the file 'LICENSE' in this package distribution
 * or at 'https://opensource.org/licenses/Apache-2.0'.
 * <p>
 * Author: Arthur Halet
 * Date: 22/03/2016
 */
public class FakeAccessManager extends AccessManager {

    private boolean isAdmin;

    public FakeAccessManager(boolean isAdmin) {
        this.isAdmin = isAdmin;
    }

    @Override
    public boolean isUserIsAdmin() {
        return this.isAdmin;
    }
}
