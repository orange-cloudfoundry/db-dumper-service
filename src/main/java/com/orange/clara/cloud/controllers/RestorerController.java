package com.orange.clara.cloud.controllers;

import com.orange.clara.cloud.dbdump.action.Restorer;
import com.orange.clara.cloud.model.DatabaseRef;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.Date;

/**
 * Copyright (C) 2015 Orange
 * <p/>
 * This software is distributed under the terms and conditions of the 'MIT'
 * license which can be found in the file 'LICENSE' in this package distribution
 * or at 'http://opensource.org/licenses/MIT'.
 * <p/>
 * Author: Arthur Halet
 * Date: 01/10/2015
 */
@RestController
public class RestorerController extends AbstractDbController {


    @Autowired
    @Qualifier(value = "restorer")
    private Restorer restorer;

    @RequestMapping(value = "/restoredb", method = RequestMethod.GET)
    public String restore(@RequestParam String dbUrlSource, @RequestParam String dbUrlTarget,
                          @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date createdAt) throws IOException, InterruptedException {
        DatabaseRef databaseRefSource = this.getDatabaseRefFromUrl(dbUrlSource, "temp");
        DatabaseRef databaseRefTarget = this.getDatabaseRefFromUrl(dbUrlTarget, "tempTarget");
        if (createdAt == null) {
            return this.restorer.restore(databaseRefSource, databaseRefTarget);
        }
        return this.restorer.restore(databaseRefSource, databaseRefTarget, createdAt);
    }
}
