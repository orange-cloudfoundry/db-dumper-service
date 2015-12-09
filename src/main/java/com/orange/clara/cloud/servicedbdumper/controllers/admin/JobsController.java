package com.orange.clara.cloud.servicedbdumper.controllers.admin;

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
 * <p/>
 * This software is distributed under the terms and conditions of the 'MIT'
 * license which can be found in the file 'LICENSE' in this package distribution
 * or at 'http://opensource.org/licenses/MIT'.
 * <p/>
 * Author: Arthur Halet
 * Date: 09/12/2015
 */
@Controller
@RequestMapping(value = "/admin")
public class JobsController {

    @Autowired
    private JobRepo jobRepo;

    @RequestMapping("/jobs")
    public String showJobs(Model model) throws IOException {
        model.addAttribute("jobs", this.jobRepo.findAll());
        return "admin/jobs";
    }

    @RequestMapping("/jobs/details/{jobId:[0-9]+}")
    public String showJob(@PathVariable Integer jobId, Model model) throws IOException {
        model.addAttribute("job", this.jobRepo.findOne(jobId));
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

    @RequestMapping("/jobs/delete/{jobId:[0-9]+}")
    public String deleteJob(@PathVariable Integer jobId, Model model) throws IOException {
        checkJob(jobId, true);
        this.jobRepo.delete(jobId);
        return "redirect:/admin/jobs";
    }

    @RequestMapping("/jobs/update/{jobId:[0-9]+}/jobevent/{event:[A-Z]+}")
    public String updateJobEventFromJob(@PathVariable Integer jobId, @PathVariable String event, Model model) throws IOException {
        checkJob(jobId, false);
        JobEvent jobEvent = JobEvent.valueOf(event);
        Job job = this.jobRepo.findOne(jobId);
        job.setJobEvent(jobEvent);
        if (jobEvent.equals(JobEvent.ERRORED)) {
            job.setErrorMessage("Put in error manually by admin.");
        }
        this.jobRepo.save(job);
        return "redirect:/admin/jobs";
    }
}
