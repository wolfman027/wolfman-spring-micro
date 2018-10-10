package com.wolfman.micro.hystrix.controller;

import com.netflix.hystrix.HystrixCommandGroupKey;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixProperty;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Random;

@RestController
public class HystrixController {

    private Random random = new Random();

    /**
     * 当{@link #helloWorld()} 方法调用超时或失败时，
     * fallback方法{@link #errorContent()}作为替代返回
     * @Author huhao
     * @DATE 9:58 2018/5/29
     * @Param []
     * @return java.lang.String
     **/
    @GetMapping("hello-world")
    @HystrixCommand(
            fallbackMethod = "errorContent",
            commandProperties = {
                    @HystrixProperty(name = "execution.isolation.thread.timeoutInMilliseconds",
                            value = "100")})
    public String helloWorld() throws Exception{
        //如果随机的时间 大于 100毫秒，那么触发容错
        int value = random.nextInt(200);
        System.out.println("hello world() costs " + value + "ms.");
        Thread.sleep(value);
        return "hello world!";
    }
    public String errorContent(){
        return "fault";
    }


    /**
     * 当{@link #helloWorld()} 方法调用超时或失败时，
     * fallback方法{@link #errorContent()}作为替代返回
     **/
    @GetMapping("hello-world2")
    public String helloWorld2(){
        return new HelloWorldCommand().execute();
    }

    /**
     * 编程方式
     **/
    private class HelloWorldCommand extends com.netflix.hystrix.HystrixCommand<String>{
        protected HelloWorldCommand() {
            super(HystrixCommandGroupKey.Factory.asKey("HelloWorld")
                    ,100);
        }

        @Override
        protected String run() throws Exception {
            // 如果随机时间 大于 100 ，那么触发容错
            int value = random.nextInt(200);
            System.out.println("helloWorld() costs " + value + " ms.");
            Thread.sleep(value);
            return "Hello,World";
        }

        @Override
        protected String getFallback() {
            return HystrixController.this.errorContent();
        }

    }






}
