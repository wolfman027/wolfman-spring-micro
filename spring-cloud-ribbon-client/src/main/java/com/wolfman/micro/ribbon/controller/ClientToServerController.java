package com.wolfman.micro.ribbon.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.stream.Collectors;

@RestController
public class ClientToServerController {

    /*@Autowired //注入RestTemplate bean
    private RestTemplate restTemplate;


    @Value("${spring.application.name}")
    private String currentServiceName;

    @Autowired
    private DiscoveryClient discoveryClient;

    //Map key serverName value URLS
    private volatile Map<String,Set<String>> targetUrlsCache = new HashMap<>();

    @Scheduled(fixedRate = 10*1000)//10秒钟更新一次
    public void updateTargetUrlsCache(){ //更新目标URL
        Map<String,Set<String>> oldTargetUrlsCache = this.targetUrlsCache;
        //获取当前所有的机器列表
        Map<String,Set<String>> newTargetUrlsCache = new HashMap<>();
                discoveryClient.getServices().forEach(serviceName ->{
            List<ServiceInstance> serviceInstances = discoveryClient.getInstances(serviceName);
            Set<String> newTargetUrls = serviceInstances
                    .stream()
                    .map(s-> s.isSecure()?"https://" + s.getHost() + ":" + s.getPort() :
                            "http://" + s.getHost() + ":" + s.getPort())
                    .collect(Collectors.toSet());
            newTargetUrlsCache.put(serviceName,newTargetUrls);
        });
        //swap
        this.targetUrlsCache = newTargetUrlsCache;
        oldTargetUrlsCache.clear();
    }

    @GetMapping("/invoke/{serviceName}/say")
    public String invokeSay(@PathVariable String serviceName,
                            @RequestParam String message){
        //服务器列表
        //快照
        List<String> targetUrls = new LinkedList<> (targetUrlsCache.get(serviceName));

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
    }
*/

}
