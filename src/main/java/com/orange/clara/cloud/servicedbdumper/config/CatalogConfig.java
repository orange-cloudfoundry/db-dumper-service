package com.orange.clara.cloud.servicedbdumper.config;

import org.cloudfoundry.community.servicebroker.model.Catalog;
import org.cloudfoundry.community.servicebroker.model.Plan;
import org.cloudfoundry.community.servicebroker.model.ServiceDefinition;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Copyright (C) 2015 Orange
 * <p/>
 * This software is distributed under the terms and conditions of the 'MIT'
 * license which can be found in the file 'LICENSE' in this package distribution
 * or at 'http://opensource.org/licenses/MIT'.
 * <p/>
 * Author: Arthur Halet
 * Date: 13/10/2015
 */
@Configuration
public class CatalogConfig {

    @Value("${vcap.application.uris[0]:localhost:8080}")
    private String appUri;

    private Map<String, Object> sdMetadata = new HashMap<String, Object>();

    @Bean
    public Catalog catalog() {
        return new Catalog(Arrays.asList(
                new ServiceDefinition(
                        "service-db-dumper",
                        "service-db-dumper",
                        "Dump and restore data from your database, ** please do not use **",
                        true,
                        true,
                        Arrays.asList(
                                new Plan("experimental",
                                        "experimental",
                                        "This is a default service-db-dumper plan.  All services are created equally.",
                                        getPlanMetadata(),
                                        true)), //TODO: change it cause set to free
                        Arrays.asList("service-db-dumper", "dump", "restore"),
                        getServiceDefinitionMetadata(),
                        null,
                        null)));
    }

/* Used by Pivotal CF console */

    private Map<String, Object> getServiceDefinitionMetadata() {
        sdMetadata.put("displayName", "service-db-dumper");
        sdMetadata.put("longDescription", "service-db-dumper");
        sdMetadata.put("providerDisplayName", "Orange");
        sdMetadata.put("documentationUrl", "");
        sdMetadata.put("supportUrl", "");
        return sdMetadata;
    }

    private Map<String, Object> getPlanMetadata() {
        Map<String, Object> planMetadata = new HashMap<String, Object>();
        planMetadata.put("costs", getCosts());
        planMetadata.put("bullets", getBullets());
        return planMetadata;
    }

    private List<Map<String, Object>> getCosts() {
        Map<String, Object> costsMap = new HashMap<String, Object>();

        Map<String, Object> amount = new HashMap<String, Object>();
        amount.put("usd", new Double(0.0));

        costsMap.put("amount", amount);
        costsMap.put("unit", "MONTHLY");

        return Arrays.asList(costsMap);
    }

    private List<String> getBullets() {
        return Arrays.asList("service-db-dumper",
                "Unlimited storage",
                "Stored in riakcs");
    }

    @PostConstruct
    public void postConstruct() {
        sdMetadata.put("imageUrl", "http://" + appUri + "/images/logo.png");
    }
}