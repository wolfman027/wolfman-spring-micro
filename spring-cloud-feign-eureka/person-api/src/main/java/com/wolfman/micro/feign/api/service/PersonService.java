package com.wolfman.micro.feign.api.service;

import com.wolfman.micro.feign.api.domain.Person;
import com.wolfman.micro.feign.api.hystrix.PersonServiceFallback;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Collection;

@FeignClient(value = "person-service", fallback = PersonServiceFallback.class)
public interface PersonService {

    /**
     * 保存
     */
    @PostMapping(value = "/person/save")
    boolean save(@RequestBody Person person);

    /**
     * 查找所有的服务
     */
    @GetMapping(value = "/person/find/all")
    Collection<Person> findAll();

}
