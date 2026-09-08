package com.hostelhub.testservice.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {

    @Value("${server.port}")
    private String port;

    @GetMapping("/api/test/hello")
    public String hello() {
        return "Hello from Test Service running on port " + port;
    }
}