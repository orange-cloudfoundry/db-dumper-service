package com.orange.clara.cloud.servicedbdumper.controllers;

import com.google.common.collect.Lists;
import com.orange.clara.cloud.servicedbdumper.exception.UserAccessRightException;
import com.orange.clara.cloud.servicedbdumper.helper.UrlForge;
import com.orange.clara.cloud.servicedbdumper.model.DatabaseRef;
import com.orange.clara.cloud.servicedbdumper.model.DbDumperServiceInstance;
import com.orange.clara.cloud.servicedbdumper.repo.DatabaseRefRepo;
import com.orange.clara.cloud.servicedbdumper.repo.DbDumperServiceInstanceRepo;
import com.orange.clara.cloud.servicedbdumper.security.UserAccessRight;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.io.IOException;
import java.util.List;

/**
 * Copyright (C) 2015 Orange
 * <p>
 * This software is distributed under the terms and conditions of the 'Apache-2.0'
 * license which can be found in the file 'LICENSE' in this package distribution
 * or at 'https://opensource.org/licenses/Apache-2.0'.
 * <p>
 * Author: Arthur Halet
 * Date: 15/10/2015
 */
@Controller
@RequestMapping(value = "/manage")
public class InterfaceController {

    @Autowired
    private DatabaseRefRepo databaseRefRepo;
    @Autowired
    private UrlForge urlForge;
    @Autowired
    private DbDumperServiceInstanceRepo instanceRepository;

    @Autowired
    @Qualifier("currency")
    private String currency;

    @Autowired
    @Qualifier("isFree")
    private Boolean isFree;

    @Autowired
    @Qualifier("userAccessRight")
    private UserAccessRight userAccessRight;

    @RequestMapping("/list")
    public String list(Model model) throws IOException, UserAccessRightException {
        List<DatabaseRef> databaseRefs = Lists.newArrayList(this.databaseRefRepo.findAll());
        model.addAttribute("databaseRefs", this.filteringDatabaseRef(databaseRefs));
        model.addAttribute("urlForge", urlForge);
        model.addAttribute("currency", currency);
        model.addAttribute("isFree", isFree);
        return "listfiles";
    }

    private List<DatabaseRef> filteringDatabaseRef(List<DatabaseRef> databaseRefs) throws UserAccessRightException {
        List<DatabaseRef> databaseRefsFinal = Lists.newArrayList();
        for (DatabaseRef databaseRef : databaseRefs) {
            if (databaseRef.isDeleted()
                    || databaseRef.getDbDumperServiceInstances() == null
                    || !this.userAccessRight.haveAccessToServiceInstance(databaseRef.getDbDumperServiceInstances())) {
                continue;
            }
            databaseRefsFinal.add(databaseRef);
        }
        return databaseRefsFinal;
    }

    @RequestMapping("/list/{instanceId}")
    public String listFromInstance(@PathVariable String instanceId, Model model) throws IOException, UserAccessRightException {
        DbDumperServiceInstance serviceInstance = instanceRepository.findOne(instanceId);
        if (serviceInstance != null && !this.userAccessRight.haveAccessToServiceInstance(serviceInstance)) {
            throw new UserAccessRightException("You don't have access to this instance");
        }
        List<DatabaseRef> databaseRefs = Lists.newArrayList();
        if (serviceInstance != null && !serviceInstance.getDatabaseRef().isDeleted()) {
            databaseRefs.add(serviceInstance.getDatabaseRef());
        }
        model.addAttribute("databaseRefs", databaseRefs);
        model.addAttribute("urlForge", urlForge);
        model.addAttribute("currency", currency);
        model.addAttribute("isFree", isFree);
        return "listfiles";
    }

    @RequestMapping("/list/database/{databaseName}")
    public String listFromDatabase(@PathVariable String databaseName, Model model) throws IOException, UserAccessRightException {
        DatabaseRef databaseRef = this.databaseRefRepo.findOne(databaseName);
        if (databaseRef == null) {
            throw new IllegalArgumentException(String.format("Cannot find database with name '%s'", databaseName));
        }
        if (!this.userAccessRight.haveAccessToServiceInstance(databaseRef.getDbDumperServiceInstances())) {
            throw new UserAccessRightException("You don't have access to this instance");
        }
        List<DatabaseRef> databaseRefs = Lists.newArrayList();
        if (!databaseRef.isDeleted()) {
            databaseRefs.add(databaseRef);
        }
        model.addAttribute("databaseRefs", databaseRefs);
        model.addAttribute("urlForge", urlForge);
        model.addAttribute("currency", currency);
        model.addAttribute("isFree", isFree);
        return "listfiles";
    }
}
