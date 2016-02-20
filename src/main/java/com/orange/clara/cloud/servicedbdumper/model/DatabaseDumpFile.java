package com.orange.clara.cloud.servicedbdumper.model;


import com.orange.clara.cloud.servicedbdumper.security.CryptoConverter;

import javax.persistence.*;
import java.io.File;
import java.util.Calendar;
import java.util.Date;

/**
 * Copyright (C) 2015 Orange
 * <p>
 * This software is distributed under the terms and conditions of the 'Apache-2.0'
 * license which can be found in the file 'LICENSE' in this package distribution
 * or at 'https://opensource.org/licenses/Apache-2.0'.
 * <p>
 * Author: Arthur Halet
 * Date: 03/06/2015
 */
@Entity
public class DatabaseDumpFile {

    protected String user;
    @Convert(converter = CryptoConverter.class)
    protected String password;
    protected Boolean showable;
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;
    private String fileName;
    @Column(name = "created_at")
    private Date createdAt;
    @ManyToOne
    @JoinColumn(name = "database_ref_id")
    private DatabaseRef databaseRef;
    private Boolean deleted;
    private Long size;

    public DatabaseDumpFile() {
        this.createdAt = Calendar.getInstance().getTime();
        this.showable = true;
        this.deleted = false;
        this.user = "";
        this.password = "";
    }

    public DatabaseDumpFile(String fileName, DatabaseRef databaseRef, String user, String password, boolean showable, long size) {
        this();
        this.fileName = fileName;
        this.user = user;
        this.password = password;
        this.showable = showable;
        this.size = size;
        this.setDatabaseRef(databaseRef);
    }

    public DatabaseDumpFile(File file, DatabaseRef databaseRef, String user, String password, boolean showable, long size) {
        this();
        this.user = user;
        this.password = password;
        this.showable = showable;
        this.size = size;
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

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Boolean getShowable() {
        return showable;
    }

    public void setShowable(Boolean showable) {
        this.showable = showable;
    }

    public Boolean isShowable() {
        return showable;
    }

    public Boolean getDeleted() {
        return deleted;
    }

    public void setDeleted(Boolean deleted) {
        this.deleted = deleted;
    }

    public Boolean isDeleted() {
        return deleted;
    }

    @PrePersist
    void createdAt() {
        this.createdAt = new Date();
    }

    @Override
    public int hashCode() {
        return id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DatabaseDumpFile that = (DatabaseDumpFile) o;

        return id == that.id;

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

    public Long getSize() {
        return size;
    }

    public void setSize(Long size) {
        this.size = size;
    }
}
