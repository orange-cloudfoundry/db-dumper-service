package com.orange.clara.cloud.servicedbdumper.repo;

import com.orange.clara.cloud.servicedbdumper.model.*;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Set;

/**
 * Copyright (C) 2015 Arthur Halet
 * <p/>
 * This software is distributed under the terms and conditions of the 'MIT'
 * license which can be found in the file 'LICENSE' in this package distribution
 * or at 'http://opensource.org/licenses/MIT'.
 * <p/>
 * Author: Arthur Halet
 * Date: 25/11/2015
 */
public interface JobRepo extends PagingAndSortingRepository<Job, Integer> {
    List<Job> findByJobTypeAndJobEvent(JobType jobType, JobEvent jobEvent);

    List<Job> findByJobEventOrderByUpdatedAtDesc(JobEvent jobEvent);

    List<Job> findByJobTypeAndJobEventAndDatabaseRefSrcAndDatabaseRefTarget(JobType jobType, JobEvent jobEvent, DatabaseRef databaseRefSrc, DatabaseRef databaseRefTarget);

    List<Job> findByJobTypeAndJobEventAndDatabaseRefSrc(JobType jobType, JobEvent jobEvent, DatabaseRef databaseRefSrc);

    List<Job> findByJobEventAndDbDumperServiceInstance(JobEvent jobEvent, DbDumperServiceInstance dbDumperServiceInstance);

    Job findFirstByDbDumperServiceInstanceOrderByUpdatedAtDesc(DbDumperServiceInstance dbDumperServiceInstance);

    @Query("select j from Job j where j.jobEvent in :jobEvents and j.dbDumperServiceInstance=:serviceInstance order by j.updatedAt desc")
    List<Job> findByDbDumperServiceInstanceInJobEventOrderByDate(@Param("serviceInstance") DbDumperServiceInstance dbDumperServiceInstance, @Param("jobEvents") Set<JobEvent> jobEvents);

    Long deleteByDbDumperServiceInstance(DbDumperServiceInstance dbDumperServiceInstance);

    Long deleteByJobEvent(JobEvent jobEvent);
}
