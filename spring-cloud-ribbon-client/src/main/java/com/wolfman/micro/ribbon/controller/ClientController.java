package com.wolfman.micro.ribbon.controller;

import org.springframework.web.bind.annotation.RestController;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.stream.Collectors;

/**
 *
 * 第一个版本，最简单版本
 *
 * 项目自己注册、自己发现
 *
 * 通过 discoveryClient 获取 currentServiceName 服务的所有注册的服务
 * 保存所有服务的请求：http://localhost:8080、http://${ip}:${port}
 *
 * 调用时，通过自己的算法，选中其中一个服务，通过 restTemplate 去请求访问
 *
 */
@RestController
public class ClientController {

    /*@Autowired //注入RestTemplate bean
    private RestTemplate restTemplate;


    @Value("${spring.application.name}")
    private String currentServiceName;

    @Autowired
    private DiscoveryClient discoveryClient;

    //线程安全
    private volatile Set<String> targetUrls = new HashSet<>();


    @Scheduled(fixedRate = 10*1000)//10秒钟更新一次
    public void updateTargetUrls(){ //更新目标URL
        Set<String> oldTargetUrls = this.targetUrls;
        //获取当前所有的机器列表
        List<ServiceInstance> serviceInstances = discoveryClient.getInstances(currentServiceName);
        //http://localhost:8080
        //http://${ip}:${port}
        Set<String> newTargetUrls = serviceInstances.stream().map(s->
            s.isSecure()?"https://" + s.getHost() + ":" + s.getPort() :
            "http://" + s.getHost() + ":" + s.getPort()
        ).collect(Collectors.toSet());
        //swap
        this.targetUrls = newTargetUrls;

        oldTargetUrls.clear();
    }

    @GetMapping("/invoke/say")
    public String invokeSay(@RequestParam String message){
        //服务器列表
        //快照
        List<String> targetUrls = new ArrayList<>(this.targetUrls);

        //轮询列表
        //选择其中一台服务器
        int size = targetUrls.size();
        //size = 3 ,indext = 0 - 2
        int index = new Random().nextInt(size);

        String targetUrl = targetUrls.get(index);
        //RestTemplate 发送请求到服务器
        //输出响应
        return restTemplate.getForObject(targetUrl+"/say?message="+message,String.class);
    }

    @GetMapping("/say")
    public String say(@RequestParam String message){
        System.out.println("接收到消息 - say：" + message);
        return "Hello,"+message;
    }

    //定义RestTemplate bean
    @Bean
    public RestTemplate restTemplate(){
        return new RestTemplate();
    }*/




}
