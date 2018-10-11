package com.wolfman.micro.feign.zk.service.feign.clients;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "spring-cloud-server-discovery-client")
public interface SayingService {

    @GetMapping("/say")
    public String say(@RequestParam("message") String message);

}
