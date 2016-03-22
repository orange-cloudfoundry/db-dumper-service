package com.orange.clara.cloud.servicedbdumper.task.boot.sequences;

import com.orange.clara.cloud.servicedbdumper.model.DbDumperPlan;
import com.orange.clara.cloud.servicedbdumper.repo.DbDumperPlanRepo;
import org.cloudfoundry.community.servicebroker.model.Catalog;
import org.cloudfoundry.community.servicebroker.model.Plan;
import org.cloudfoundry.community.servicebroker.model.ServiceDefinition;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.fest.assertions.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

/**
 * Copyright (C) 2016 Arthur Halet
 * <p>
 * This software is distributed under the terms and conditions of the 'MIT'
 * license which can be found in the file 'LICENSE' in this package distribution
 * or at 'http://opensource.org/licenses/MIT'.
 * <p>
 * Author: Arthur Halet
 * Date: 21/03/2016
 */
public class BootSequencePlanTest {
    private final static String currency = "euros";
    private final static String dbDumperPlanId1 = "1K";
    private final static Long size1 = 1024L;
    private final static Long size2 = 2048L;
    private final static String dbDumperPlanId2 = "2K";
    private final static Float costPlanId1 = 10F;
    private final static Float costPlanId2 = 35F;
    @InjectMocks
    BootSequencePlan bootSequencePlan;
    @Mock
    Catalog catalog;
    @Mock
    DbDumperPlanRepo dbDumperPlanRepo;
    private Plan plan1;
    private Plan plan2;
    private DbDumperPlan dbDumperPlan1;
    private DbDumperPlan dbDumperPlan2;

    @Before
    public void init() {
        initMocks(this);
        plan1 = this.createPlan(dbDumperPlanId1, costPlanId1);
        plan2 = this.createPlan(dbDumperPlanId2, costPlanId2);
        dbDumperPlan1 = new DbDumperPlan(dbDumperPlanId1);
        dbDumperPlan2 = new DbDumperPlan(dbDumperPlanId2);
        bootSequencePlan.currency = currency;
        bootSequencePlan.isFree = false;
        List<Plan> plans = Arrays.asList(plan1, plan2);
        List<ServiceDefinition> serviceDefinitions = Arrays.asList(new ServiceDefinition("1", "name-1", "desc", true, plans));
        when(catalog.getServiceDefinitions()).thenReturn(serviceDefinitions);
        when(dbDumperPlanRepo.findOne(dbDumperPlanId1)).thenReturn(dbDumperPlan1);
        when(dbDumperPlanRepo.findOne(dbDumperPlanId2)).thenReturn(dbDumperPlan2);
    }

    @Test
    public void when_running_sequence_it_should_update_db_dumper_plan_from_plan_in_catalog() {
        this.assertEmpty(dbDumperPlan1);
        this.assertEmpty(dbDumperPlan2);
        this.bootSequencePlan.runSequence();
        assertEqualToPlan(dbDumperPlan1, plan1, costPlanId1, size1);
        assertEqualToPlan(dbDumperPlan2, plan2, costPlanId2, size2);
    }

    @Test
    public void when_running_sequence_and_service_is_free_it_should_update_db_dumper_plan_from_plan_in_catalog_with_no_cost() {
        this.assertEmpty(dbDumperPlan1);
        this.assertEmpty(dbDumperPlan2);
        this.bootSequencePlan.isFree = true;
        this.bootSequencePlan.runSequence();
        assertEqualToPlan(dbDumperPlan1, plan1, 0.0F, size1);
        assertEqualToPlan(dbDumperPlan2, plan2, 0.0F, size2);
    }

    @Test
    public void when_running_sequence_and_currency_not_found_it_should_update_db_dumper_plan_from_plan_in_catalog_with_no_cost() {
        this.assertEmpty(dbDumperPlan1);
        this.assertEmpty(dbDumperPlan2);
        this.bootSequencePlan.currency = "dollar";
        this.bootSequencePlan.runSequence();
        assertEqualToPlan(dbDumperPlan1, plan1, 0.0F, size1);
        assertEqualToPlan(dbDumperPlan2, plan2, 0.0F, size2);
    }

    private void assertEqualToPlan(DbDumperPlan dbDumperPlan, Plan plan, Float cost, Long size) {
        assertThat(dbDumperPlan.getCost()).isEqualTo(cost);
        assertThat(dbDumperPlan.getName()).isEqualTo(plan.getName());
        assertThat(dbDumperPlan.getSize()).isEqualTo(size);
    }

    private void assertEmpty(DbDumperPlan dbDumperPlan) {
        assertThat(dbDumperPlan.getCost()).isEqualTo(0.0F);
        assertThat(dbDumperPlan.getName()).isNull();
        assertThat(dbDumperPlan.getSize()).isNull();
    }

    private Plan createPlan(String id, float cost) {
        return new Plan(id, id, "description for plan " + id, getPlanMetadata(cost));
    }

    private Map<String, Object> getPlanMetadata(float cost) {
        Map<String, Object> planMetadata = new HashMap<String, Object>();
        planMetadata.put("costs", getCosts(cost));
        planMetadata.put("bullets", Arrays.asList("my plan"));
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
}