package com.orange.clara.cloud.servicedbdumper.controllers;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.ui.Model;

/**
 * Copyright (C) 2016 Orange
 * <p>
 * This software is distributed under the terms and conditions of the 'Apache-2.0'
 * license which can be found in the file 'LICENSE' in this package distribution
 * or at 'https://opensource.org/licenses/Apache-2.0'.
 * <p>
 * Author: Arthur Halet
 * Date: 21/09/2016
 */
public abstract class AbstractController {

    @Value("${app.maven.version:0.0.1}")
    private String version;

    public void addDefaultAttribute(Model model) {
        model.addAttribute("appVersion", version);
    }
}
