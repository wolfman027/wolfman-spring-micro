package com.wolfman.micro.discovery.zk.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SayController {

    @GetMapping("/say")
    public String say(@RequestParam("message") String message){
        System.out.println("serverController接收到消息 - say：" + message);
        return "Hello,"+message;
    }


}
