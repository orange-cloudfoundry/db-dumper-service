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
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "database_ref_src_id")
    private DatabaseRef databaseRefSrc;

    @ManyToOne
    @JoinColumn(name = "database_ref_target_id")
    private DatabaseRef databaseRefTarget;

    @Enumerated(EnumType.STRING)
    private JobEvent jobEvent;

    @Enumerated(EnumType.STRING)
    private JobType jobType;

    private Date updatedAt;

    public Job() {
        this.updatedAt = Calendar.getInstance().getTime();
        this.jobEvent = JobEvent.START;
    }

    public Job(JobType jobType, DatabaseRef databaseRefSrc) {
        this();
        this.jobType = jobType;
        this.databaseRefSrc = databaseRefSrc;
    }

    public Job(JobType jobType, DatabaseRef databaseRefSrc, DatabaseRef databaseRefTarget) {
        this(jobType, databaseRefSrc);
        this.databaseRefTarget = databaseRefTarget;
    }

    public Job(JobType jobType, DatabaseRef databaseRefSrc, DatabaseRef databaseRefTarget, Date updatedAt) {
        this(jobType, databaseRefSrc, databaseRefTarget);
        this.updatedAt = updatedAt;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public DatabaseRef getDatabaseRefSrc() {
        return databaseRefSrc;
    }

    public void setDatabaseRefSrc(DatabaseRef databaseRefSrc) {
        this.databaseRefSrc = databaseRefSrc;
    }

    public DatabaseRef getDatabaseRefTarget() {
        return databaseRefTarget;
    }

    public void setDatabaseRefTarget(DatabaseRef databaseRefTarget) {
        this.databaseRefTarget = databaseRefTarget;
    }

    public JobType getJobType() {
        return jobType;
    }

    public void setJobType(JobType jobType) {
        this.jobType = jobType;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }

    public JobEvent getJobEvent() {
        return jobEvent;
    }

    public void setJobEvent(JobEvent jobEvent) {
        this.jobEvent = jobEvent;
    }
}
