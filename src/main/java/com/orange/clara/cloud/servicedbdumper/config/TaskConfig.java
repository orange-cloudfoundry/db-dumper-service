package com.orange.clara.cloud.servicedbdumper.config;

import com.orange.clara.cloud.servicedbdumper.task.asynctask.CreateDumpTask;
import com.orange.clara.cloud.servicedbdumper.task.asynctask.DeleteDumpTask;
import com.orange.clara.cloud.servicedbdumper.task.asynctask.RestoreDumpTask;
import com.orange.clara.cloud.servicedbdumper.task.job.JobFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Copyright (C) 2015 Orange
 * <p/>
 * This software is distributed under the terms and conditions of the 'MIT'
 * license which can be found in the file 'LICENSE' in this package distribution
 * or at 'http://opensource.org/licenses/MIT'.
 * <p/>
 * Author: Arthur Halet
 * Date: 26/11/2015
 */
@Configuration
public class TaskConfig {

    @Bean
    public DeleteDumpTask deleteDumpTask() {
        return new DeleteDumpTask();
    }

    @Bean
    public CreateDumpTask createDumpTask() {
        return new CreateDumpTask();
    }

    @Bean
    public RestoreDumpTask restoreDumpTask() {
        return new RestoreDumpTask();
    }

    @Bean
    public JobFactory jobFactory() {
        return new JobFactory();
    }
}
