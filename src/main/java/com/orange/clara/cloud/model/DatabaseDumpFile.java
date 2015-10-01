package com.orange.clara.cloud.model;


import javax.persistence.*;
import java.io.File;
import java.util.Date;

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

    @Column(name = "created_at")
    private Date createdAt;

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

    public void setFileName(File file) {
        this.fileName = file.getName();
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public File getFile() {
        return new File(this.fileName);
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    @PrePersist
    void createdAt() {
        this.createdAt = new Date();
    }

    @Override
    public String toString() {
        return "DatabaseDumpFile{" +
                "id=" + id +
                ", fileName='" + fileName + '\'' +
                ", createdAt=" + createdAt +
                ", databaseRef=" + databaseRef +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DatabaseDumpFile that = (DatabaseDumpFile) o;

        return id == that.id;

    }

    @Override
    public int hashCode() {
        return id;
    }
}
