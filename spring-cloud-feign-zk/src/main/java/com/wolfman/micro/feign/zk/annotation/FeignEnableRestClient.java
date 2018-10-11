package com.wolfman.micro.feign.zk.annotation;

import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
@Import(FeignRestClientsRegistrar.class)
public @interface FeignEnableRestClient {

    /**
     * 指定 @RestClient 接口
     * @return
     */
    Class<?>[] clients() default {};

}

