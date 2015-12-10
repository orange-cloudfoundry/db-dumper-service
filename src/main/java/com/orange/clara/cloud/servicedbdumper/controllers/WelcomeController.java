package com.orange.clara.cloud.servicedbdumper.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Copyright (C) 2015 Orange
 * <p/>
 * This software is distributed under the terms and conditions of the 'MIT'
 * license which can be found in the file 'LICENSE' in this package distribution
 * or at 'http://opensource.org/licenses/MIT'.
 * <p/>
 * Author: Arthur Halet
 * Date: 10/12/2015
 */
@Controller
public class WelcomeController {
    @RequestMapping("/manage")
    public String welcome(Model model) {
        return "welcome";
    }

    @RequestMapping({"/manage/admin", "/admin/control"})
    public String welcomeAdmin(Model model) {
        return "welcome";
    }
}
