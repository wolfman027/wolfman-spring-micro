package com.wolfman.micro.feign.zk;

import com.wolfman.micro.feign.zk.annotation.FeignEnableRestClient;
import com.wolfman.micro.feign.zk.service.feign.clients.SayingService;
import com.wolfman.micro.feign.zk.service.rest.clients.FeignSayingRestService;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication  //标准 Spring boot 应用
@EnableDiscoveryClient//激活服务发现客户端
@EnableFeignClients(clients = SayingService.class) //激活服务调用并引入FeignClient
@FeignEnableRestClient(clients = FeignSayingRestService.class)  //引入 @FeignRestClient
public class FeignClientApplication {

    public static void main(String[] args) {
        new SpringApplicationBuilder(FeignClientApplication.class)
                .web(WebApplicationType.SERVLET)
                .run(args);
    }
}
