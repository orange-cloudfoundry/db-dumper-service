package com.orange.clara.cloud.controllers;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {
    @RequestMapping("/lapin")
    public String index() {
        return "Greetings from Spring Boot!";
    }
}
