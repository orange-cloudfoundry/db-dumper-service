package com.orange.clara.cloud.servicedbdumper.config;

import com.google.common.collect.Lists;
import com.orange.clara.cloud.servicedbdumper.helper.ByteFormat;
import org.cloudfoundry.community.servicebroker.model.Catalog;
import org.cloudfoundry.community.servicebroker.model.DashboardClient;
import org.cloudfoundry.community.servicebroker.model.Plan;
import org.cloudfoundry.community.servicebroker.model.ServiceDefinition;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.text.ParseException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Copyright (C) 2015 Orange
 * <p>
 * This software is distributed under the terms and conditions of the 'Apache-2.0'
 * license which can be found in the file 'LICENSE' in this package distribution
 * or at 'https://opensource.org/licenses/Apache-2.0'.
 * <p>
 * Author: Arthur Halet
 * Date: 13/10/2015
 */
@Configuration
public class CatalogConfig {

    @Value("#{${use.ssl:false} ? 'https://' : 'http://'}${vcap.application.uris[0]:localhost:8080}")
    private String appUri;
    @Value("${security.oauth2.client.clientSecret:fakeClientSecret}")
    private String clientSecret;
    @Value("${security.oauth2.client.clientId:fakeClientId}")
    private String clientId;
    @Value("${service.definition.id:db-dumper-service}")
    private String serviceDefinitionId;

    @Value("${app.maven.version:0.0.1}")
    private String version;

    @Value("${dump.delete.expiration.days:5}")
    private Integer dumpDeleteExpirationDays;

    private Map<String, Object> sdMetadata = new HashMap<String, Object>();

    @Value("#{'${service.definition.quota:experimental}'.split(',')}")
    private List<String> quotas;

    @Value("${service.definition.currency:usd}")
    private String currency;

    @Value("#{'${service.definition.cost.formulas:quota}'.split(',')}")
    private List<String> formulas;


    @Value("${service.definition.cost.mb:0.1}")
    private Float costOneMega;

    @Value("${service.definition.is.free:true}")
    private Boolean isFree;

    @Bean
    public String appUri() {
        return this.appUri;
    }

    @Bean
    public Integer dumpDeleteExpirationDays() {
        return this.dumpDeleteExpirationDays;
    }

    @Bean
    public Boolean isFree() {
        return this.isFree;
    }

    @Bean
    public String currency() {
        return this.currency;
    }

    @Bean
    public Catalog catalog() throws ScriptException, ParseException {

        return new Catalog(Arrays.asList(
                new ServiceDefinition(
                        this.serviceDefinitionId,
                        this.serviceDefinitionId,
                        "Dump and restore data from your database",
                        true,
                        true,
                        this.getPlans(), //TODO: change it cause set to free
                        Arrays.asList("db-dumper-service", "dump", "restore"),
                        getServiceDefinitionMetadata(),
                        null,
                        this.getDashboardClient())));
    }

    private DashboardClient getDashboardClient() {
        return new DashboardClient(this.clientId, this.clientSecret, this.appUri + "/login");
    }

    private float getDefaultCost() {
        return this.costOneMega / 1024 / 1024;
    }

    private float getCostFromQuota(String quota, String formula) throws ParseException, ScriptException {
        long size = ByteFormat.parse(quota);
        String formulaInjected = formula.replaceAll("quota", String.valueOf(size * this.getDefaultCost()));
        ScriptEngineManager factory = new ScriptEngineManager();
        ScriptEngine engine = factory.getEngineByName("JavaScript");
        Object eval = engine.eval(formulaInjected);
        if (eval instanceof Integer) {
            return new Float((Integer) eval);
        }
        if (eval instanceof Float) {
            return (Float) eval;
        }
        return Math.round((Double) eval);
    }

    public List<Plan> getPlans() throws ScriptException, ParseException {
        if (this.quotas.size() == 1
                && (this.quotas.get(0).equals("experimental") || this.quotas.get(0).equals("unlimited"))) {
            return Arrays.asList(this.createDefaultPlan(this.quotas.get(0)));
        }
        if (this.isFree) {
            return Arrays.asList(this.createDefaultPlan("unlimited"));
        }
        List<Plan> plans = Lists.newArrayList();
        int formulasSize = this.formulas.size();
        for (int i = 0; i < this.quotas.size(); i++) {
            String quota = this.quotas.get(i);
            String formula = this.formulas.get(i % formulasSize);
            plans.add(new Plan(this.serviceDefinitionId + "-plan-" + quota,
                    quota,
                    "This is a db-dumper-service plan.  All services are created equally.",
                    getPlanMetadata(this.getCostFromQuota(quota, formula)),
                    false));
        }
        return plans;
    }

    private Plan createDefaultPlan(String name) {
        return new Plan(this.serviceDefinitionId + "-plan-" + name,
                name,
                "This is a default db-dumper-service plan.  All services are created equally.",
                getPlanMetadata(0),
                true);
    }

    private Map<String, Object> getServiceDefinitionMetadata() {
        sdMetadata.put("displayName", "db-dumper-service");
        sdMetadata.put("longDescription", "db-dumper-service v" + version);
        sdMetadata.put("providerDisplayName", "Orange");
        sdMetadata.put("documentationUrl", "https://github.com/orange-cloudfoundry/db-dumper-service/tree/v" + version);
        sdMetadata.put("supportUrl", "https://github.com/orange-cloudfoundry/db-dumper-service/tree/v" + version);
        return sdMetadata;
    }

    private Map<String, Object> getPlanMetadata(float cost) {
        Map<String, Object> planMetadata = new HashMap<String, Object>();
        planMetadata.put("costs", getCosts(cost));
        planMetadata.put("bullets", getBullets());
        return planMetadata;
    }

    private List<Map<String, Object>> getCosts(float cost) {
        Map<String, Object> costsMap = new HashMap<String, Object>();

        Map<String, Object> amount = new HashMap<String, Object>();
        amount.put(this.currency, cost);

        costsMap.put("amount", amount);
        costsMap.put("unit", "MONTHLY");

        return Arrays.asList(costsMap);
    }

    private List<String> getBullets() {
        return Arrays.asList("db-dumper-service",
                "Stored in S3 filer",
                "Deleted dumps are stored during " + this.dumpDeleteExpirationDays + "days before really deleted");
    }

    @PostConstruct
    public void postConstruct() {
        sdMetadata.put("imageUrl", appUri + "/images/logo.png");
    }
}