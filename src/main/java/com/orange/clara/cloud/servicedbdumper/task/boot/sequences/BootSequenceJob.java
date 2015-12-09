package com.orange.clara.cloud.servicedbdumper.task.boot.sequences;

import com.google.common.collect.Sets;
import com.orange.clara.cloud.servicedbdumper.model.JobEvent;
import com.orange.clara.cloud.servicedbdumper.repo.JobRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

/**
 * Copyright (C) 2015 Orange
 * <p/>
 * This software is distributed under the terms and conditions of the 'MIT'
 * license which can be found in the file 'LICENSE' in this package distribution
 * or at 'http://opensource.org/licenses/MIT'.
 * <p/>
 * Author: Arthur Halet
 * Date: 09/12/2015
 */
@Component
@Order(2)
public class BootSequenceJob implements BootSequence {

    private final static String ERROR_MESSAGE = "Job was running when service-db-dumper was stopped";
    @Autowired
    private JobRepo jobRepo;

    @Override
    @Transactional
    public void runSequence() {
        JobEvent[] jobEvents = new JobEvent[]{JobEvent.RUNNING, JobEvent.START};
        Set<JobEvent> jobEventSet = Sets.newHashSet(jobEvents);
        this.jobRepo.updateJobFromJobEventSetToJobEventWithErrorMessage(JobEvent.ERRORED, jobEventSet, ERROR_MESSAGE);
    }
}
