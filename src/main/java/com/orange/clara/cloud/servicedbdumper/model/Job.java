package com.orange.clara.cloud.servicedbdumper.model;

import com.orange.clara.cloud.servicedbdumper.converter.MetadataConverter;

import javax.persistence.*;
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

    @ManyToOne
    @JoinColumn(name = "service_instance_id")
    private DbDumperServiceInstance dbDumperServiceInstance;

    @Enumerated(EnumType.STRING)
    private JobEvent jobEvent;

    @Enumerated(EnumType.STRING)
    private JobType jobType;

    @Convert(converter = MetadataConverter.class)
    private Metadata metadata;

    private Date updatedAt;

    private Date dumpDate;

    private String errorMessage;

    public Job() {
        this.updatedAt = Calendar.getInstance().getTime();
        this.jobEvent = JobEvent.START;
    }

    public Job(JobType jobType, DatabaseRef databaseRefSrc, DbDumperServiceInstance dbDumperServiceInstance) {
        this();
        this.dbDumperServiceInstance = dbDumperServiceInstance;
        this.jobType = jobType;
        this.databaseRefSrc = databaseRefSrc;
    }

    public Job(JobType jobType, DatabaseRef databaseRefSrc, DbDumperServiceInstance dbDumperServiceInstance, Metadata metadata) {
        this();
        this.dbDumperServiceInstance = dbDumperServiceInstance;
        this.jobType = jobType;
        this.databaseRefSrc = databaseRefSrc;
    }

    public Job(JobType jobType, DatabaseRef databaseRefSrc, DatabaseRef databaseRefTarget, DbDumperServiceInstance dbDumperServiceInstance) {
        this(jobType, databaseRefSrc, dbDumperServiceInstance);
        this.databaseRefTarget = databaseRefTarget;
    }

    public Job(JobType jobType, DatabaseRef databaseRefSrc, DatabaseRef databaseRefTarget, Date dumpDate, DbDumperServiceInstance dbDumperServiceInstance) {
        this(jobType, databaseRefSrc, databaseRefTarget, dbDumperServiceInstance);
        this.dumpDate = dumpDate;
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
        this.updatedAt = Calendar.getInstance().getTime();
        this.databaseRefSrc = databaseRefSrc;
    }

    public DatabaseRef getDatabaseRefTarget() {
        return databaseRefTarget;
    }

    public void setDatabaseRefTarget(DatabaseRef databaseRefTarget) {
        this.updatedAt = Calendar.getInstance().getTime();
        this.databaseRefTarget = databaseRefTarget;
    }

    public JobType getJobType() {
        return jobType;
    }

    public void setJobType(JobType jobType) {
        this.updatedAt = Calendar.getInstance().getTime();
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
        this.updatedAt = Calendar.getInstance().getTime();
        this.jobEvent = jobEvent;
    }

    public Date getDumpDate() {
        return dumpDate;
    }

    public void setDumpDate(Date dumpDate) {
        this.updatedAt = Calendar.getInstance().getTime();
        this.dumpDate = dumpDate;
    }

    public DbDumperServiceInstance getDbDumperServiceInstance() {
        return dbDumperServiceInstance;
    }

    public void setDbDumperServiceInstance(DbDumperServiceInstance dbDumperServiceInstance) {
        this.updatedAt = Calendar.getInstance().getTime();
        this.dbDumperServiceInstance = dbDumperServiceInstance;
    }

    public Metadata getMetadata() {
        return metadata;
    }

    public void setMetadata(Metadata metadata) {
        this.metadata = metadata;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.updatedAt = Calendar.getInstance().getTime();
        this.errorMessage = errorMessage;
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Job job = (Job) o;

        return !(id != null ? !id.equals(job.id) : job.id != null);

    }
}
