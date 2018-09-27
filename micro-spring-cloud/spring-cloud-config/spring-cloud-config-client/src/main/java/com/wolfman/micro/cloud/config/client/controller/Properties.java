package com.wolfman.micro.cloud.config.client.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RefreshScope
public class Properties {

    @Value("${age}")
    private String myName;

    @GetMapping("/my-name")
    public String getName(){
        return myName;
    }

}
