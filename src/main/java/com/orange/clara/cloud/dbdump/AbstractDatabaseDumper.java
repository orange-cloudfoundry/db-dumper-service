package com.orange.clara.cloud.dbdump;

import com.orange.clara.cloud.model.DatabaseRef;
import org.springframework.beans.factory.annotation.Required;

import java.io.File;

public abstract class AbstractDatabaseDumper implements DatabaseDumper {
    protected File binaryDump;
    protected File binaryRestore;
    protected DatabaseRef databaseRef;

    public AbstractDatabaseDumper() {

    }

    public AbstractDatabaseDumper(File binaryDump, File binaryRestore) {
        this.binaryDump = binaryDump;
        this.binaryRestore = binaryRestore;
    }

    public File getBinaryDump() {
        return binaryDump;
    }

    @Required
    public void setBinaryDump(File binaryDump) {
        this.binaryDump = binaryDump;
    }

    public File getBinaryRestore() {
        return binaryRestore;
    }

    @Required
    public void setBinaryRestore(File binaryRestore) {
        this.binaryRestore = binaryRestore;
    }

    public DatabaseRef getDatabaseRef() {
        return databaseRef;
    }

    @Override
    public void setDatabaseRef(DatabaseRef databaseRef) {
        this.databaseRef = databaseRef;
    }

}
