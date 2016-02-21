package com.orange.clara.cloud.servicedbdumper.task.boot.sequences;

import com.orange.clara.cloud.servicedbdumper.helper.ByteFormat;
import com.orange.clara.cloud.servicedbdumper.model.DbDumperPlan;
import com.orange.clara.cloud.servicedbdumper.repo.DbDumperPlanRepo;
import org.cloudfoundry.community.servicebroker.model.Catalog;
import org.cloudfoundry.community.servicebroker.model.Plan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * Copyright (C) 2016 Orange
 * <p>
 * This software is distributed under the terms and conditions of the 'Apache-2.0'
 * license which can be found in the file 'LICENSE' in this package distribution
 * or at 'https://opensource.org/licenses/Apache-2.0'.
 * <p>
 * Author: Arthur Halet
 * Date: 21/02/2016
 */
@Component
@Order(4)
public class BootSequencePlan implements BootSequence {

    @Autowired
    private Catalog catalog;

    @Autowired
    @Qualifier("currency")
    private String currency;

    @Autowired
    private DbDumperPlanRepo dbDumperPlanRepo;

    @Autowired
    @Qualifier("isFree")
    private Boolean isFree;

    @Override
    public void runSequence() {
        for (Plan plan : this.catalog.getServiceDefinitions().get(0).getPlans()) {
            this.createOrUpdateDbDumperPlan(plan);
        }
    }

    private void createOrUpdateDbDumperPlan(Plan plan) {
        DbDumperPlan dbDumperPlan;
        dbDumperPlan = this.dbDumperPlanRepo.findOne(plan.getId());
        if (dbDumperPlan == null) {
            dbDumperPlan = new DbDumperPlan(plan.getId());
        }
        dbDumperPlan.setName(plan.getName());
        Long size = null;
        try {
            size = ByteFormat.parse(plan.getName());
        } catch (Exception e) {
            size = null;
        }
        dbDumperPlan.setSize(size);
        if (this.isFree) {
            dbDumperPlan.setCost(0.0F);
        } else {
            dbDumperPlan.setCost(this.getCost(plan));
        }
        this.dbDumperPlanRepo.save(dbDumperPlan);
    }

    private Float getCost(Plan plan) {
        Map<String, Object> metadata = plan.getMetadata();
        if (!metadata.containsKey("costs")) {
            return 0.0F;
        }
        Map<String, Object> costs = ((List<Map<String, Object>>) metadata.get("costs")).get(0);
        if (!costs.containsKey("amount")) {
            return 0.0F;
        }
        Map<String, Object> amount = (Map<String, Object>) costs.get("amount");
        if (!amount.containsKey(this.currency)) {
            System.out.println("this is not found");
            return 0.0F;
        }
        return (Float) amount.get(this.currency);
    }
}
