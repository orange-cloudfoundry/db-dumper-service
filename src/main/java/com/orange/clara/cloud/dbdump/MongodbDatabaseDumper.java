package com.orange.clara.cloud.dbdump;

import java.io.File;

public class MongodbDatabaseDumper extends AbstractDatabaseDumper implements DatabaseDumper {
    public MongodbDatabaseDumper(File binaryDump, File binaryRestore) {
        super(binaryDump, binaryRestore);
    }

    @Override
    public Boolean handles(String type) {
        return type.equals("mongodb");
    }

    @Override
    public String[] getDumpCommandLine(String inputPath) {
        return String.format(
                "%s --host %s --port %s --username %s --password %s --db %s --out %s",
                this.binaryDump.getAbsolutePath(),
                this.databaseRef.getHost(),
                this.databaseRef.getPort(),
                this.databaseRef.getUser(),
                this.databaseRef.getPassword(),
                this.databaseRef.getDatabaseName(),
                inputPath
        ).split(" ");
    }

    @Override
    public String[] getRestoreCommandLine(String outputPath) {
        return String.format(
                "%s --host %s --port %s --username %s --password %s --db %s %s",
                this.binaryDump.getAbsolutePath(),
                this.databaseRef.getHost(),
                this.databaseRef.getPort(),
                this.databaseRef.getUser(),
                this.databaseRef.getPassword(),
                this.databaseRef.getDatabaseName(),
                outputPath
        ).split(" ");
    }
}
