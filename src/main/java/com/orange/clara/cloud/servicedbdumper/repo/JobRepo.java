package com.orange.clara.cloud.servicedbdumper.repo;

import com.orange.clara.cloud.servicedbdumper.model.*;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

/**
 * Copyright (C) 2015 Orange
 * <p/>
 * This software is distributed under the terms and conditions of the 'Apache-2.0'
 * license which can be found in the file 'LICENSE' in this package distribution
 * or at 'https://opensource.org/licenses/Apache-2.0'.
 * <p/>
 * Author: Arthur Halet
 * Date: 25/11/2015
 */
@Repository
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

    @Modifying
    @Query("UPDATE Job j SET j.jobEvent = :toJobEvent, j.errorMessage = :errorMessage where j.jobEvent in :jobEvents")
    int updateJobFromJobEventSetToJobEventWithErrorMessage(@Param("toJobEvent") JobEvent toJobEvent, @Param("jobEvents") Set<JobEvent> jobEvents, @Param("errorMessage") String errorMessage);

}
