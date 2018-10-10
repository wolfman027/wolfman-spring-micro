package com.wolfman.micro.hystrix.aop;

import com.wolfman.micro.hystrix.annotation.CircuitBreaker;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.aop.aspectj.MethodInvocationProceedingJoinPoint;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;
import java.lang.reflect.Method;
import java.util.concurrent.*;

/**
 *  高级版本 + 注解 aop
 */
@Aspect
@Component
public class HystrixAdvancedAnnotationControllerAspect {

    private ExecutorService executorService = Executors.newFixedThreadPool(20);

    private Object doInvoke(ProceedingJoinPoint point, String message, long timeout) throws Throwable {

        Future<Object> future = executorService.submit(()->{
            Object returnValue = null;
            try{
                returnValue = point.proceed(new String[]{message});
            }catch (Throwable throwable){
            }
            return returnValue;
        });

        //100 毫秒 超时
        Object returnValue = null;
        try{
            returnValue = future.get(timeout, TimeUnit.MILLISECONDS);
        }catch (TimeoutException e){
            future.cancel(true);//取消执行
            returnValue = errorContent("");
            //throw e;
        }
        return returnValue;
    }


//    @Around(value = "execution(* com.wolfman.micro.hystrix.controller.HystrixAdvancedAnnotationController.say(..)) && " +
//            "args(message) && @annotation(circuitBreaker)")
//    public Object advancedAnnotationSayInTimeout(ProceedingJoinPoint point, String message, CircuitBreaker circuitBreaker) throws Throwable {
//        long timeout = circuitBreaker.timeout();
//        return doInvoke(point,message,timeout);
//    }

    /**
     * 利用反射来做
     * @param point
     * @param message
     * @return
     * @throws Throwable
     */
    @Around("execution(* com.wolfman.micro.hystrix.controller.HystrixAdvancedAnnotationController.say(..)) && " +
            "args(message)")
    public Object advancedAnnotationSayInTimeout(ProceedingJoinPoint point, String message) throws Throwable {
        long timeout = -1;
        if (point instanceof MethodInvocationProceedingJoinPoint){
            MethodInvocationProceedingJoinPoint methodPoint = (MethodInvocationProceedingJoinPoint) point;
            MethodSignature signature = (MethodSignature) methodPoint.getSignature();
            Method method = signature.getMethod();
            CircuitBreaker circuitBreaker = method.getAnnotation(CircuitBreaker.class);
            timeout = circuitBreaker.timeout();
        }
        return doInvoke(point,message,timeout);
    }


    public String errorContent(String message){
        return "Fault";
    }

    @PreDestroy
    public void destory(){
        executorService.shutdown();
    }

}
