package com.orange.clara.cloud.servicedbdumper.repo;

import com.orange.clara.cloud.servicedbdumper.model.DatabaseRef;
import com.orange.clara.cloud.servicedbdumper.model.Job;
import com.orange.clara.cloud.servicedbdumper.model.JobEvent;
import com.orange.clara.cloud.servicedbdumper.model.JobType;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;

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

    List<Job> findByJobEvent(JobEvent jobEvent);

    List<Job> findByJobTypeAndJobEventAndDatabaseRefSrcAndDatabaseRefTarget(JobType jobType, JobEvent jobEvent, DatabaseRef databaseRefSrc, DatabaseRef databaseRefTarget);

    List<Job> findByJobTypeAndJobEventAndDatabaseRefSrc(JobType jobType, JobEvent jobEvent, DatabaseRef databaseRefSrc);

    Long deleteByJobEvent(JobEvent jobEvent);
}
