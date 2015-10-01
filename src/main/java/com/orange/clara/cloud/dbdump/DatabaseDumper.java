package com.orange.clara.cloud.dbdump;

import com.orange.clara.cloud.model.DatabaseRef;

/**
 * Copyright (C) 2015 Orange
 * <p/>
 * This software is distributed under the terms and conditions of the 'MIT'
 * license which can be found in the file 'LICENSE' in this package distribution
 * or at 'http://opensource.org/licenses/MIT'.
 * <p/>
 * Author: Arthur Halet
 * Date: 03/06/2015
 */
public interface DatabaseDumper {

    void setDatabaseRef(DatabaseRef databaseRef);

    String[] getDumpCommandLine();

    String[] getRestoreCommandLine();

}
