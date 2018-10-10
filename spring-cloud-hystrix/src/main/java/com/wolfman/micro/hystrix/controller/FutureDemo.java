package com.wolfman.micro.hystrix.controller;

import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class FutureDemo {

    public static void main(String[] args) {
        Random random = new Random();
        ExecutorService service = Executors.newFixedThreadPool(1);
        Future future = service.submit(()->{ //正常流程
            //如果随机的时间 大于 100毫秒，那么触发容错
            int value = random.nextInt(200);
            System.out.println("hello world() costs " + value + "ms.");
            Thread.sleep(value);
            System.out.println("Hello,World!");
            return "Hello,World!";
        });
        try {
            future.get(100, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            //超时流程
            System.out.println("超时保护");
        }
        service.shutdown();
    }
}
