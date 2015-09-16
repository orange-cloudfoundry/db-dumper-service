package com.orange.clara.cloud.model;


import javax.persistence.*;
import java.io.File;

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
@Entity
public class DatabaseDumpFile {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;

    private String fileName;

    @ManyToOne
    @JoinColumn(name = "database_ref_id")
    private DatabaseRef databaseRef;

    public DatabaseDumpFile() {

    }

    public DatabaseDumpFile(String fileName, DatabaseRef databaseRef) {
        this.fileName = fileName;
        this.setDatabaseRef(databaseRef);
    }

    public DatabaseDumpFile(File file, DatabaseRef databaseRef) {
        this.setFileName(file);
        this.setDatabaseRef(databaseRef);
    }

    public DatabaseRef getDatabaseRef() {
        return databaseRef;
    }

    public void setDatabaseRef(DatabaseRef databaseRef) {
        this.databaseRef = databaseRef;
        databaseRef.addDatabaseDumpFile(this);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getFileName() {
        return this.fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public void setFileName(File file) {
        this.fileName = file.getName();
    }

    public File getFile() {
        return new File(this.fileName);
    }
}
