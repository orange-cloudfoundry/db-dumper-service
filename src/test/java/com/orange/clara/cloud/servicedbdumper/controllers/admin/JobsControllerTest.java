package com.orange.clara.cloud.servicedbdumper.controllers.admin;

import com.github.dandelion.core.web.DandelionFilter;
import com.orange.clara.cloud.servicedbdumper.Application;
import com.orange.clara.cloud.servicedbdumper.config.Routes;
import com.orange.clara.cloud.servicedbdumper.model.Job;
import com.orange.clara.cloud.servicedbdumper.model.JobEvent;
import com.orange.clara.cloud.servicedbdumper.model.JobType;
import com.orange.clara.cloud.servicedbdumper.repo.JobRepo;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.mock.web.MockFilterConfig;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.util.NestedServletException;

import java.util.Arrays;

import static org.fest.assertions.Assertions.assertThat;
import static org.fest.assertions.Fail.fail;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

/**
 * Copyright (C) 2016 Arthur Halet
 * <p>
 * This software is distributed under the terms and conditions of the 'MIT'
 * license which can be found in the file 'LICENSE' in this package distribution
 * or at 'http://opensource.org/licenses/MIT'.
 * <p>
 * Author: Arthur Halet
 * Date: 24/03/2016
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = {Application.class})
@WebAppConfiguration
@ActiveProfiles("local")
public class JobsControllerTest {
    protected DandelionFilter dandelionFilter;
    private MockMvc mockMvc;
    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private JobRepo jobRepo;

    private Job jobStarted;
    private Job jobRunning;
    private Job jobScheduled;

    @Before
    public void setup() throws Exception {

        this.dandelionFilter = new DandelionFilter();
        this.dandelionFilter.init(new MockFilterConfig());

        this.mockMvc = webAppContextSetup(webApplicationContext).addFilter(this.dandelionFilter).build();
        jobScheduled = new Job();
        jobScheduled.setJobType(JobType.CREATE_DUMP);
        jobScheduled.setJobEvent(JobEvent.SCHEDULED);

        jobStarted = new Job();
        jobStarted.setJobType(JobType.CREATE_DUMP);

        jobRunning = new Job();
        jobRunning.setJobEvent(JobEvent.RUNNING);
        jobRunning.setJobType(JobType.CREATE_DUMP);

        jobRepo.save(Arrays.asList(jobStarted, jobRunning, jobScheduled));
    }

    @After
    public void clean() {
        jobRepo.deleteAll();
    }

    @Test
    public void when_admin_wants_to_have_the_list_of_jobs_it_should_return_all_jobs() throws Exception {
        mockMvc.perform(get(Routes.JOB_CONTROL_ROOT))
                .andExpect(model().attribute("jobs", hasItem(
                        allOf(
                                hasProperty("id", is(jobStarted.getId())),
                                hasProperty("jobEvent", is(JobEvent.START))
                        )
                )))
                .andExpect(model().attribute("jobs", hasItem(
                        allOf(
                                hasProperty("id", is(jobRunning.getId())),
                                hasProperty("jobEvent", is(JobEvent.RUNNING))
                        )
                )));
    }

    @Test
    public void when_admin_wants_to_see_a_particular_job_it_should_return_this_job() throws Exception {
        mockMvc.perform(get(Routes.JOB_CONTROL_ROOT + Routes.JOB_CONTROL_DETAILS_ROOT + "/" + jobStarted.getId()))
                .andExpect(model().attribute("job", allOf(
                        hasProperty("id", is(jobStarted.getId())),
                        hasProperty("jobEvent", is(JobEvent.START))
                )));
    }

    @Test
    public void when_admin_wants_to_delete_job_it_should_remove_the_job_from_database_and_redirect_to_list_of_jobs() throws Exception {
        mockMvc.perform(get(Routes.JOB_CONTROL_ROOT + Routes.JOB_CONTROL_DELETE_ROOT + "/" + jobScheduled.getId()))
                .andExpect(redirectedUrl(Routes.JOB_CONTROL_ROOT));
        assertThat(jobRepo.findOne(jobScheduled.getId())).isNull();
    }

    @Test
    public void when_admin_wants_to_delete_a_running_job_it_should_throw_an_exception() throws Exception {
        try {
            mockMvc.perform(get(Routes.JOB_CONTROL_ROOT + Routes.JOB_CONTROL_DELETE_ROOT + "/" + jobRunning.getId()));
            fail("It should throw an IllegalArgumentException");
        } catch (NestedServletException e) {
            assertThat(e.getCause()).isInstanceOf(IllegalArgumentException.class);
        }
        try {
            mockMvc.perform(get(Routes.JOB_CONTROL_ROOT + Routes.JOB_CONTROL_DELETE_ROOT + "/" + jobStarted.getId()));
            fail("It should throw an IllegalArgumentException");
        } catch (NestedServletException e) {
            assertThat(e.getCause()).isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Test
    public void when_admin_wants_to_delete_or_update_an_non_existing_job_it_should_throw_an_exception() throws Exception {
        try {
            mockMvc.perform(get(Routes.JOB_CONTROL_ROOT + Routes.JOB_CONTROL_DELETE_ROOT + "/10000"));
            fail("It should throw an IllegalArgumentException");
        } catch (NestedServletException e) {
            assertThat(e.getCause()).isInstanceOf(IllegalArgumentException.class);
        }
        try {
            mockMvc.perform(get(Routes.JOB_CONTROL_ROOT + Routes.JOB_CONTROL_UPDATE_ROOT + "/10000" + "/jobevent/" + JobEvent.ERRORED));
            fail("It should throw an IllegalArgumentException");
        } catch (NestedServletException e) {
            assertThat(e.getCause()).isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Test
    public void when_admin_wants_to_update_job_event_it_should_update_this_job_with_the_new_event_and_redirect_to_list_of_jobs() throws Exception {
        mockMvc.perform(get(Routes.JOB_CONTROL_ROOT + Routes.JOB_CONTROL_UPDATE_ROOT + "/" + jobStarted.getId() + "/jobevent/" + JobEvent.FINISHED))
                .andExpect(redirectedUrl(Routes.JOB_CONTROL_ROOT));
        Job job = jobRepo.findOne(jobStarted.getId());
        assertThat(job.getJobEvent()).isEqualTo(JobEvent.FINISHED);
    }

    @Test
    public void when_admin_wants_to_put_in_error_a_job_manually_it_should_update_this_job_with_errored_event_and_set_en_error_message_to_the_job_and_redirect_to_list_of_jobs() throws Exception {
        mockMvc.perform(get(Routes.JOB_CONTROL_ROOT + Routes.JOB_CONTROL_UPDATE_ROOT + "/" + jobStarted.getId() + "/jobevent/" + JobEvent.ERRORED))
                .andExpect(redirectedUrl(Routes.JOB_CONTROL_ROOT));
        Job job = jobRepo.findOne(jobStarted.getId());
        assertThat(job.getJobEvent()).isEqualTo(JobEvent.ERRORED);
        assertThat(job.getErrorMessage()).isNotEmpty();
    }
}