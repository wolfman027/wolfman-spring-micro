package com.wolfman.micro.feign.zk.service.rest.clients;

import com.wolfman.micro.feign.zk.annotation.FeignRestClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignRestClient(name = "${saying.rest.service.name}")
public interface FeignSayingRestService {

    @GetMapping("/say")
    public String say(@RequestParam("message") String message);

}
