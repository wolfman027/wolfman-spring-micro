package com.wolfman.micro.hystrix.annotation;

import java.lang.annotation.*;

@Target(ElementType.METHOD)//标注在方法
@Retention(RetentionPolicy.RUNTIME) //运行时保存注解信息
@Documented
public @interface CircuitBreaker {

    /**
     * 超时时间
     * @return 设置超时时间
     */
    long timeout();

}
