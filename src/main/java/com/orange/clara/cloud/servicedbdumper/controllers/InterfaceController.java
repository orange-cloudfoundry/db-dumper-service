package com.orange.clara.cloud.servicedbdumper.controllers;

import com.google.common.collect.Lists;
import com.orange.clara.cloud.servicedbdumper.model.DatabaseRef;
import com.orange.clara.cloud.servicedbdumper.model.DbDumperServiceInstance;
import com.orange.clara.cloud.servicedbdumper.repo.DatabaseRefRepo;
import com.orange.clara.cloud.servicedbdumper.repo.DbDumperServiceInstanceRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.io.IOException;
import java.util.List;

/**
 * Copyright (C) 2015 Orange
 * <p/>
 * This software is distributed under the terms and conditions of the 'MIT'
 * license which can be found in the file 'LICENSE' in this package distribution
 * or at 'http://opensource.org/licenses/MIT'.
 * <p/>
 * Author: Arthur Halet
 * Date: 15/10/2015
 */
@Controller
@RequestMapping(value = "/manage")
public class InterfaceController {

    @Autowired
    private DatabaseRefRepo databaseRefRepo;

    @Autowired
    private DbDumperServiceInstanceRepo instanceRepository;


    @RequestMapping("/list")
    public String list(Model model) throws IOException {
        List<DatabaseRef> databaseRefs = Lists.newArrayList(this.databaseRefRepo.findAll());
        model.addAttribute("databaseRefs", this.filteringDatabaseRef(databaseRefs));
        return "listfiles";
    }

    private List<DatabaseRef> filteringDatabaseRef(List<DatabaseRef> databaseRefs) {
        List<DatabaseRef> databaseRefsFinal = Lists.newArrayList();
        for (DatabaseRef databaseRef : databaseRefs) {
            if (databaseRef.isDeleted()) {
                continue;
            }
            databaseRefsFinal.add(databaseRef);
        }
        return databaseRefsFinal;
    }

    @RequestMapping("/list/{instanceId}")
    public String listFromInstance(@PathVariable String instanceId, Model model) throws IOException {
        DbDumperServiceInstance serviceInstance = instanceRepository.findOne(instanceId);
        List<DatabaseRef> databaseRefs = Lists.newArrayList();
        if (serviceInstance != null && !serviceInstance.getDatabaseRef().isDeleted()) {
            databaseRefs.add(serviceInstance.getDatabaseRef());
        }
        model.addAttribute("databaseRefs", databaseRefs);
        return "listfiles";
    }

    @RequestMapping("/list/database/{databaseName}")
    public String listFromDatabase(@PathVariable String databaseName, Model model) throws IOException {
        DatabaseRef databaseRef = this.databaseRefRepo.findOne(databaseName);
        if (databaseRef == null) {
            throw new IllegalArgumentException(String.format("Cannot find database with name '%s'", databaseName));
        }
        List<DatabaseRef> databaseRefs = Lists.newArrayList();
        if (!databaseRef.isDeleted()) {
            databaseRefs.add(databaseRef);
        }
        model.addAttribute("databaseRefs", databaseRefs);
        return "listfiles";
    }
}
