package com.wolfman.micro.hystrix.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Random;
import java.util.concurrent.*;

/**
 *  低级版本 + 有容错实现
 */
@RestController
@RequestMapping("/hystrix/second")
public class HystrixSecondController {

    private final static Random random = new Random();

    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    @GetMapping("/say")
    public String say(@RequestParam String message) throws Exception {
        Future<String> future = executorService.submit(()->{
            return doSay(message);
        });
        //100 毫秒超时
        String returnValue = null;
        try {
            returnValue =  future.get(100, TimeUnit.MILLISECONDS);
        }catch (InterruptedException | ExecutionException | TimeoutException e){
            //超级容错 = 执行错误 或
            returnValue = errorContent(message);
        }
        return returnValue;
    }

    private String doSay(String message) throws InterruptedException {
        int value = random.nextInt(200);
        System.out.println("say() costs " + value + "ms.");
        Thread.sleep(value);
        String returnValue = "Say："+message;
        return returnValue;
    }

    public String errorContent(String message){
        return "Fault";
    }


}
