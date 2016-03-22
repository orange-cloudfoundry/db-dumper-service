package com.orange.clara.cloud.servicedbdumper.controllers;

import com.orange.clara.cloud.servicedbdumper.config.Routes;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

/**
 * Copyright (C) 2015 Orange
 * <p>
 * This software is distributed under the terms and conditions of the 'Apache-2.0'
 * license which can be found in the file 'LICENSE' in this package distribution
 * or at 'https://opensource.org/licenses/Apache-2.0'.
 * <p>
 * Author: Arthur Halet
 * Date: 10/12/2015
 */
@Controller
public class WelcomeController {

    @RequestMapping("/")
    public ModelAndView defaultRoute() {
        return new ModelAndView("redirect:" + Routes.MANAGE_ROOT);
    }

    @RequestMapping(Routes.MANAGE_ROOT)
    public String welcome(Model model) {
        return "welcome";
    }

    @RequestMapping({Routes.MANAGE_ADMIN_ROOT, Routes.MANAGE_ADMIN_ROOT_ALTERNATIVE})
    public String welcomeAdmin(Model model) {
        return "welcome";
    }
}
