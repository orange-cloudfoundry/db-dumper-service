package com.orange.clara.cloud.dbdump;

import com.orange.clara.cloud.model.DatabaseRef;

public interface DatabaseDumper {

    void setDatabaseRef(DatabaseRef databaseRef);

    String[] getDumpCommandLine(String inputPath);

    String[] getRestoreCommandLine(String outputPath);

}
