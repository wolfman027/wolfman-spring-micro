package com.wolfman.micro.ribbon.controller;

import com.wolfman.micro.ribbon.annotation.CustomizedLoadBalanced;
import com.wolfman.micro.ribbon.loadbalance.LoadBalanceRequestInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.Collection;

@RestController
public class ClientAnnotationToServerController {

    @Autowired //注入 Ribbon RestTemplate bean
    @Qualifier
    private RestTemplate restTemplate;

    @Autowired //注入自定义 RestTemplate bean
    @CustomizedLoadBalanced
    private RestTemplate myRestTemplate;

    @GetMapping("/loadBalance/invoke/{serviceName}/say")
    public String lbInvokeSay(@PathVariable String serviceName,
                              @RequestParam String message){

        return restTemplate.getForObject("http://" + serviceName+"/say?message="+message,String.class);
    }

    @GetMapping("/invoke/{serviceName}/say")
    public String invokeSay(@PathVariable String serviceName,
                            @RequestParam String message){

        return myRestTemplate.getForObject("/" + serviceName+"/say?message="+message,String.class);
    }


    @GetMapping("/say")
    public String say(@RequestParam String message){
        System.out.println("接收到消息 - say：" + message);
        return "Hello,"+message;
    }

    @Bean
    public ClientHttpRequestInterceptor interceptor(){
        return new LoadBalanceRequestInterceptor();
    }


    //Ribbon RestTemplate Bean
    @LoadBalanced
    @Bean
    public RestTemplate restTemplate(){
        return new RestTemplate();
    }

    //自定义 RestTemplate bean
    @Bean
//    @Qualifier
//    @Autowired
    @CustomizedLoadBalanced
    public RestTemplate myRestTemplate(){//依赖注入

//        RestTemplate restTemplate = new RestTemplate();
//
//        restTemplate.setInterceptors(Arrays.asList(interceptor));

        return new RestTemplate();
    }


    @Bean
    @Autowired
    public Object putMySelfInterceptor(@CustomizedLoadBalanced Collection<RestTemplate> restTemplates,
                                       ClientHttpRequestInterceptor interceptor){
        restTemplates.forEach(r -> {
            r.setInterceptors(Arrays.asList(interceptor));
        });
        return new Object();
    }



}
