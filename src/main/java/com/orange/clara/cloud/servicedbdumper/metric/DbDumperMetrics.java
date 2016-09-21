package com.orange.clara.cloud.servicedbdumper.metric;

import com.google.common.collect.Lists;
import com.orange.clara.cloud.servicedbdumper.model.JobEvent;
import com.orange.clara.cloud.servicedbdumper.model.JobType;
import com.orange.clara.cloud.servicedbdumper.repo.DatabaseDumpFileRepo;
import com.orange.clara.cloud.servicedbdumper.repo.DbDumperServiceInstanceBindingRepo;
import com.orange.clara.cloud.servicedbdumper.repo.DbDumperServiceInstanceRepo;
import com.orange.clara.cloud.servicedbdumper.repo.JobRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.endpoint.PublicMetrics;
import org.springframework.boot.actuate.metrics.Metric;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;

/**
 * Copyright (C) 2016 Orange
 * <p>
 * This software is distributed under the terms and conditions of the 'Apache-2.0'
 * license which can be found in the file 'LICENSE' in this package distribution
 * or at 'https://opensource.org/licenses/Apache-2.0'.
 * <p>
 * Author: Arthur Halet
 * Date: 21/09/2016
 */
@Service
public class DbDumperMetrics implements PublicMetrics {
    private final String namespace = "dbdumper";
    @Autowired
    private JobRepo jobRepo;

    @Autowired
    private DatabaseDumpFileRepo dumpFileRepo;

    @Autowired
    private DbDumperServiceInstanceRepo serviceInstanceRepo;

    @Autowired
    private DbDumperServiceInstanceBindingRepo serviceInstanceBindingRepo;

    @Override
    public Collection<Metric<?>> metrics() {
        List<Metric<?>> metrics = Lists.newArrayList();

        metrics.add(new Metric<Integer>(
                namespace + ".job.dump.create.success",
                jobRepo.findByJobTypeAndJobEvent(JobType.CREATE_DUMP, JobEvent.FINISHED).size()));
        metrics.add(new Metric<Integer>(
                namespace + ".job.dump.create.failed",
                jobRepo.findByJobTypeAndJobEvent(JobType.CREATE_DUMP, JobEvent.ERRORED).size()));
        metrics.add(new Metric<Integer>(
                namespace + ".job.dump.restore.success",
                jobRepo.findByJobTypeAndJobEvent(JobType.RESTORE_DUMP, JobEvent.FINISHED).size()));
        metrics.add(new Metric<Integer>(
                namespace + ".job.dump.restore.failed",
                jobRepo.findByJobTypeAndJobEvent(JobType.RESTORE_DUMP, JobEvent.ERRORED).size()));
        metrics.add(new Metric<Integer>(
                namespace + ".job.dump.delete.success",
                jobRepo.findByJobTypeAndJobEvent(JobType.DELETE_DUMPS, JobEvent.FINISHED).size()));
        metrics.add(new Metric<Integer>(
                namespace + ".job.dump.delete.failed",
                jobRepo.findByJobTypeAndJobEvent(JobType.DELETE_DUMPS, JobEvent.ERRORED).size()));

        metrics.add(new Metric<Integer>(
                namespace + ".number.dumps",
                dumpFileRepo.findByDeletedFalseOrderByCreatedAtAsc().size()));
        metrics.add(new Metric<Integer>(
                namespace + ".number.dumps.deleted",
                dumpFileRepo.findByDeletedTrueOrderByDeletedAtAsc().size()));

        metrics.add(new Metric<Integer>(
                namespace + ".number.service.instances",
                Lists.newArrayList(serviceInstanceRepo.findAll()).size()));
        metrics.add(new Metric<Integer>(
                namespace + ".number.service.instances.deleted",
                serviceInstanceRepo.findByDeletedTrue().size()));

        metrics.add(new Metric<Integer>(
                namespace + ".number.service.bindings",
                Lists.newArrayList(serviceInstanceBindingRepo.findAll()).size()));

        metrics.add(new Metric<Integer>(
                namespace + ".number.jobs",
                Lists.newArrayList(jobRepo.findAll()).size()));
        metrics.add(new Metric<Integer>(
                namespace + ".number.jobs.running",
                jobRepo.findByJobEventOrderByUpdatedAtDesc(JobEvent.RUNNING).size()));
        metrics.add(new Metric<Integer>(
                namespace + ".number.jobs.scheduled",
                jobRepo.findByJobEventOrderByUpdatedAtDesc(JobEvent.SCHEDULED).size()));
        metrics.add(new Metric<Integer>(
                namespace + ".number.jobs.failed",
                jobRepo.findByJobEventOrderByUpdatedAtDesc(JobEvent.ERRORED).size()));
        metrics.add(new Metric<Integer>(
                namespace + ".number.jobs.started",
                jobRepo.findByJobEventOrderByUpdatedAtDesc(JobEvent.START).size()));
        metrics.add(new Metric<Integer>(
                namespace + ".number.jobs.paused",
                jobRepo.findByJobEventOrderByUpdatedAtDesc(JobEvent.PAUSED).size()));
        metrics.add(new Metric<Integer>(
                namespace + ".number.jobs.finished",
                jobRepo.findByJobEventOrderByUpdatedAtDesc(JobEvent.FINISHED).size()));
        return metrics;
    }
}
