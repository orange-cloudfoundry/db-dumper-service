package com.orange.clara.cloud.servicedbdumper.controllers.admin;

import com.orange.clara.cloud.servicedbdumper.config.Routes;
import com.orange.clara.cloud.servicedbdumper.controllers.AbstractController;
import com.orange.clara.cloud.servicedbdumper.model.Job;
import com.orange.clara.cloud.servicedbdumper.model.JobEvent;
import com.orange.clara.cloud.servicedbdumper.repo.JobRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.io.IOException;

/**
 * Copyright (C) 2015 Orange
 * <p>
 * This software is distributed under the terms and conditions of the 'Apache-2.0'
 * license which can be found in the file 'LICENSE' in this package distribution
 * or at 'https://opensource.org/licenses/Apache-2.0'.
 * <p>
 * Author: Arthur Halet
 * Date: 09/12/2015
 */
@Controller
@RequestMapping(value = Routes.JOB_CONTROL_ROOT)
public class JobsController extends AbstractController {

    @Autowired
    private JobRepo jobRepo;

    @RequestMapping("")
    public String showJobs(Model model) throws IOException {
        model.addAttribute("jobs", this.jobRepo.findAll());
        this.addDefaultAttribute(model);
        return "admin/jobs";
    }

    @RequestMapping(Routes.JOB_CONTROL_DETAILS_ROOT + "/{jobId:[0-9]+}")
    public String showJob(@PathVariable Integer jobId, Model model) throws IOException {
        model.addAttribute("job", this.jobRepo.findOne(jobId));
        this.addDefaultAttribute(model);
        return "admin/jobdetails";
    }

    private void checkJob(int jobId, boolean checkIsRunning) {
        Job job = this.jobRepo.findOne(jobId);
        if (job == null) {
            throw new IllegalArgumentException(String.format("Cannot find job with id '%s'", jobId));
        }
        if (checkIsRunning && (job.getJobEvent().equals(JobEvent.RUNNING) || job.getJobEvent().equals(JobEvent.START))) {
            throw new IllegalArgumentException(String.format("The job '%s' is running.", job.getId()));
        }
    }

    @RequestMapping(Routes.JOB_CONTROL_DELETE_ROOT + "/{jobId:[0-9]+}")
    public String deleteJob(@PathVariable Integer jobId, Model model) throws IOException {
        checkJob(jobId, true);
        this.jobRepo.delete(jobId);
        return "redirect:" + Routes.JOB_CONTROL_ROOT;
    }

    @RequestMapping(Routes.JOB_CONTROL_UPDATE_ROOT + "/{jobId:[0-9]+}/jobevent/{event:[A-Z]+}")
    public String updateJobEventFromJob(@PathVariable Integer jobId, @PathVariable String event, Model model) throws IOException {
        checkJob(jobId, false);
        JobEvent jobEvent = JobEvent.valueOf(event);
        Job job = this.jobRepo.findOne(jobId);
        job.setJobEvent(jobEvent);
        if (jobEvent.equals(JobEvent.ERRORED)) {
            job.setErrorMessage("Put in error manually by admin.");
        }
        this.jobRepo.save(job);
        return "redirect:" + Routes.JOB_CONTROL_ROOT;
    }
}
