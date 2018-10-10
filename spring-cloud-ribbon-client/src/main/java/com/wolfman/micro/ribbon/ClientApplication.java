package com.wolfman.micro.ribbon;

import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableDiscoveryClient
@EnableScheduling
public class ClientApplication {

    public static void main(String[] args) {

        new SpringApplicationBuilder(ClientApplication.class)
                .web(WebApplicationType.SERVLET)
                .run(args);
    }

}
