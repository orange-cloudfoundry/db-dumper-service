package com.orange.clara.cloud.dbdump;

import java.io.File;

public class PostgresqlDatabaseDumper extends AbstractDatabaseDumper implements DatabaseDumper {
    public PostgresqlDatabaseDumper(File binaryDump, File binaryRestore) {
        super(binaryDump, binaryRestore);
    }

    @Override
    public Boolean handles(String type) {
        return type.equals("postgres");
    }

    @Override
    public String[] getDumpCommandLine(String inputPath) {
        return String.format(
                "%s --dbname=postgresql://%s:%s@%s:%s/%s -f %s",
                this.binaryDump.getAbsolutePath(),
                this.databaseRef.getUser(),
                this.databaseRef.getPassword(),
                this.databaseRef.getHost(),
                this.databaseRef.getPort(),
                this.databaseRef.getDatabaseName(),
                inputPath
        ).split(" ");
    }

    @Override
    public String[] getRestoreCommandLine(String outputPath) {
        return String.format(
                "%s --dbname=postgresql://%s:%s@%s:%s/%s -f %s",
                this.binaryDump.getAbsolutePath(),
                this.databaseRef.getUser(),
                this.databaseRef.getPassword(),
                this.databaseRef.getHost(),
                this.databaseRef.getPort(),
                this.databaseRef.getDatabaseName(),
                outputPath
        ).split(" ");
    }
}
