package com.wolfman.micro.discovery.zk;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient  //尽可能使用 @EnableDiscoveryClient
public class ZkDiscoveryClientAppliction {

    public static void main(String[] args) {
        SpringApplication.run(ZkDiscoveryClientAppliction.class,args);
    }

}
