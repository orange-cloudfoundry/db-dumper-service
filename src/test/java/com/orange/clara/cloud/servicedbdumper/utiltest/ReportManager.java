package com.orange.clara.cloud.servicedbdumper.utiltest;

import com.google.common.collect.Lists;

import java.util.List;

/**
 * Copyright (C) 2016 Orange
 * <p>
 * This software is distributed under the terms and conditions of the 'Apache-2.0'
 * license which can be found in the file 'LICENSE' in this package distribution
 * or at 'https://opensource.org/licenses/Apache-2.0'.
 * <p>
 * Author: Arthur Halet
 * Date: 20/05/2016
 */
public class ReportManager {
    private static ReportManager instance;
    private List<ReportIntegration> reportIntegrations;

    private ReportManager() {
        this.reportIntegrations = Lists.newArrayList();
    }

    public static ReportManager getInstance() {
        if (instance == null) {
            instance = new ReportManager();
        }
        return instance;
    }

    public static ReportIntegration createReportIntegration(String reportName) {
        ReportIntegration reportIntegration = new ReportIntegration(reportName);
        getInstance().reportIntegrations.add(reportIntegration);
        return reportIntegration;
    }

    public static List<ReportIntegration> getAllReports() {
        return getInstance().reportIntegrations;
    }

}
