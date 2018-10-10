package com.wolfman.micro.hystrix.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Random;

/**
 *  高级版本
 */
@RestController
@RequestMapping("/hystrix/advanced")
public class HystrixAdvancedController {

    private final static Random random = new Random();

    @GetMapping("/say")
    public String say(@RequestParam String message) throws Exception {
        return doSay(message);
    }

    private String doSay(String message) throws InterruptedException {
        int value = random.nextInt(200);
        System.out.println("say() costs " + value + "ms.");
        Thread.sleep(value);
        String returnValue = "Say："+message;
        System.out.println(returnValue);
        return returnValue;
    }

}
