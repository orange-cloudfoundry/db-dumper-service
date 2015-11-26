package com.orange.clara.cloud.servicedbdumper.model;

import javax.persistence.*;
import java.util.Calendar;
import java.util.Date;

/**
 * Copyright (C) 2015 Orange
 * <p/>
 * This software is distributed under the terms and conditions of the 'MIT'
 * license which can be found in the file 'LICENSE' in this package distribution
 * or at 'http://opensource.org/licenses/MIT'.
 * <p/>
 * Author: Arthur Halet
 * Date: 25/11/2015
 */
@Entity
public class Job {
    @Enumerated(EnumType.STRING)
    protected JobState jobState;
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "database_ref_id")
    private DatabaseRef databaseRef;

    private Date updatedAt;

    public Job(JobState jobState, DatabaseRef databaseRef) {
        this.jobState = jobState;
        this.databaseRef = databaseRef;
        this.updatedAt = Calendar.getInstance().getTime();
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public DatabaseRef getDatabaseRef() {
        return databaseRef;
    }

    public void setDatabaseRef(DatabaseRef databaseRef) {
        this.databaseRef = databaseRef;
    }

    public JobState getJobState() {
        return jobState;
    }

    public void setJobState(JobState jobState) {
        this.jobState = jobState;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }
}
