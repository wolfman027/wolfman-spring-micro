package com.wolfman.micro.hystrix.aop;

import com.wolfman.micro.hystrix.annotation.SemaphoreCircuitBreaker;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;

/**
 * 高级版本 + 注解信号量方式
 */
@Aspect
@Component
public class HystrixAdvancedAnnotationSemaphoreControllerAspect {

    private ExecutorService executorService = Executors.newFixedThreadPool(20);

    private volatile Semaphore semaphore = null;

    @Around("execution(* com.wolfman.micro.hystrix.controller.HystrixAdvancedAnnotationSemaphoreController.say(..)) && " +
            "args(message) && @annotation(circuitBreaker)")
    public Object advancedAnnotationSayInTimeout(ProceedingJoinPoint point,
                                                 String message,
                                                 SemaphoreCircuitBreaker circuitBreaker) throws Throwable {
        int value = circuitBreaker.value();
        if (semaphore == null){
            semaphore = new Semaphore(value);
        }
        Object returnValue = null;
        try{
            if (semaphore.tryAcquire()){
                returnValue = point.proceed(new Object[]{message});
                Thread.sleep(1000);
            }else {
                returnValue = errorContent(message);
            }
        }finally {
            semaphore.release();
        }
        return returnValue;
    }

    public String errorContent(String message){
        return "Fault";
    }

    @PreDestroy
    public void destory(){
        executorService.shutdown();
    }


}
