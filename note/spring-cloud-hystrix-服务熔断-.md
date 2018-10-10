##  spring cloud hystrix 服务熔断

### 一 短路，熔断，服务保护

![](https://github.com/wolfJava/wolfman-spring-micro/blob/master/spring-cloud-hystrix/img/hystrix-1.jpg?raw=true)

![](https://github.com/wolfJava/wolfman-spring-micro/blob/master/spring-cloud-hystrix/img/hystrix-2.jpg?raw=true)

![](https://github.com/wolfJava/wolfman-spring-micro/blob/master/spring-cloud-hystrix/img/hystrix-3.jpg?raw=true)

#### 1 服务短路（CircuitBreaker）

QPS: Query Per Second 每秒钟的查询

TPS: Transaction Per Second 每秒钟的事务

QPS: 经过全链路压测，计算单机极限QPS，集群 QPS = 单机 QPS * 集群机器数量 * 可靠性比率

全链路压测除了压极限QPS，还有错误数量

全链路：一个完整的业务流程操作

JMeter：可调整型比较灵活

### 二 Hystrix Client

**官网：https://github.com/Netflix/Hystrix**

Hystrix 可以是服务端实现，也可以是客户端实现，类似于 AOP 封装：正常逻辑、容错处理。

#### 1 激活 Hystrix

通过@EnableHystrix激活

Hystrix 配置信息wiki：https://github.com/Netflix/Hystrix/wiki/Configuration

**注解方式实现（Annotation）：**

~~~java
@RestController
public class HystrixController {

    private Random random = new Random();

    /**
     * 当{@link #helloWorld()} 方法调用超时或失败时，
     * fallback方法{@link #errorContent()}作为替代返回
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

}

~~~

**编程方式实现（Annotation）：**

~~~java
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
~~~

**对比 其他 Java 执行方式：**

##### 1.1 Future

~~~java
public class FutureDemo {
    public static void main(String[] args) {
        Random random = new Random();
        ExecutorService service = Executors.newFixedThreadPool(1);
        Future future = service.submit(()->{ //正常流程
            //如果随机的时间 大于 100毫秒，那么触发容错
            int value = random.nextInt(200);
            System.out.println("hello world() costs " + value + "ms.");
            Thread.sleep(value);
            System.out.println("Hello,World!");
            return "Hello,World!";
        });
        try {
            future.get(100, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            //超时流程
            System.out.println("超时保护");
        }
        service.shutdown();
    }
}
~~~

##### 1.2 RxJava

~~~java
public class RxJavaDemo {
    public static void main(String[] args) {
        Random random = new Random();
        Single.just("hello,world!")//just 发布数据
                .subscribeOn(Schedulers.immediate())//订阅线程池 immediate = Thread.currentThread();
                .subscribe(new Observer<String>() {
                    @Override
                    public void onCompleted() {//正常结束流程
                        System.out.println("执行结束！");
                    }
                    @Override
                    public void onError(Throwable throwable) {//异常流程
                        System.out.println("熔断保护！");
                    }
                    @Override
                    public void onNext(String s) { //数据消费 s= hello,world!
                        int value = random.nextInt(200);
                        if (value>100){
                            throw new RuntimeException("Timeout!");
                        }
                        System.out.println("hello world() costs " + value + "ms.");
                    }
                });
    }
}
~~~

#### 2 Health Endpoint(`/health`)

~~~java
{
  status: "UP",
  diskSpace: {
    status: "UP",
    total: 500096983040,
    free: 304113217536,
    threshold: 10485760
  },
  refreshScope: {
    status: "UP"
  },
  hystrix: {
    status: "UP"
  }
}
~~~

#### 3 激活熔断保护

`@EnableCircuitBreaker` 激活 ：`@EnableHystrix ` + Spring Cloud 功能

`@EnableHystrix` 激活，没有一些 Spring Cloud 功能，如 `/hystrix.stream` 端点

#### 4 Hystrix Endpoint(`/hystrix.stream`)

~~~java
data: {
    "type": "HystrixThreadPool",
    "name": "HystrixDemoController",
    "currentTime": 1509545957972,
    "currentActiveCount": 0,
    "currentCompletedTaskCount": 14,
    "currentCorePoolSize": 10,
    "currentLargestPoolSize": 10,
    "currentMaximumPoolSize": 10,
    "currentPoolSize": 10,
    "currentQueueSize": 0,
    "currentTaskCount": 14,
    "rollingCountThreadsExecuted": 5,
    "rollingMaxActiveThreads": 1,
    "rollingCountCommandRejections": 0,
    "propertyValue_queueSizeRejectionThreshold": 5,
    "propertyValue_metricsRollingStatisticalWindowInMilliseconds": 10000,
    "reportingHosts": 1
}
~~~

#### 5 Spring Cloud Hystrix Dashboard

激活：`@EnableHystrixDashboard`

~~~text
localhost:7070/hystrix/monitor?stream=http%3A%2F%2Flocalhost%3A8080%2Fhystrix.stream
~~~

~~~java
@SpringBootApplication
@EnableHystrixDashboard
public class SpringCloudHystrixDashboardDemoApplication {
	public static void main(String[] args) {
		SpringApplication.run(SpringCloudHystrixDashboardDemoApplication.class, args);
	}
}
~~~

### 三 实现服务熔断（Future）

#### 1 Spring Cloud Hystrix Client

> 注意：方法签名
>
> - 访问限定符
> - 方法返回类型
> - 方法名称
> - 方法参数
>   - 方法数量
>   - 方法类型 + 顺序
>   - ~~方法名称（编译时预留，IDE，Debug）~~

#### 2 低级版本（无容错实现）

~~~java
/**
 *  低级版本 + 无容错实现
 */
@RestController
@RequestMapping("/hystrix")
public class HystrixServerFirstController {

    private final static Random random = new Random();

    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    /**
     * 简易版本
     * @param message
     * @return
     * @throws InterruptedException
     */
    @GetMapping("/say")
    public String say(@RequestParam String message) throws Exception {
        Future<String> future = executorService.submit(()->{
            return doSay(message);
        });
        //100 毫秒超时
        String returnValue =  future.get(100, TimeUnit.MILLISECONDS);
        return returnValue;
    }

    private String doSay(String message) throws InterruptedException {
        int value = random.nextInt(200);
        System.out.println("say() costs " + value + "ms.");
        Thread.sleep(value);
        String returnValue = "Say："+message;
        return returnValue;
    }

    public String errorContent(String message){
        return "Fault";
    }

}
~~~

#### 3 低级版本（有容错实现）

~~~java
/**
 *  低级版本 + 有容错实现
 */
@RestController
@RequestMapping("/hystrix/second")
public class HystrixSecondController {

    private final static Random random = new Random();

    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    @GetMapping("/say")
    public String say(@RequestParam String message) throws Exception {
        Future<String> future = executorService.submit(()->{
            return doSay(message);
        });
        //100 毫秒超时
        String returnValue = null;
        try {
            returnValue =  future.get(100, TimeUnit.MILLISECONDS);
        }catch (InterruptedException | ExecutionException | TimeoutException e){
            //超级容错 = 执行错误 或
            returnValue = errorContent(message);
        }
        return returnValue;
    }

    private String doSay(String message) throws InterruptedException {
        int value = random.nextInt(200);
        System.out.println("say() costs " + value + "ms.");
        Thread.sleep(value);
        String returnValue = "Say："+message;
        return returnValue;
    }
    
    public String errorContent(String message){
        return "Fault";
    }
}
~~~

#### 4 中级版本

~~~java
/**
 *  中级版本
 */
@RestController
@RequestMapping("/hystrix/middle")
public class HystrixMiddleController {

    private final static Random random = new Random();

    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    @GetMapping("/say")
    public String say(@RequestParam String message) throws Exception {
        Future<String> future = executorService.submit(()->{
            return doSay(message);
        });
        //100 毫秒 超时
        String returnValue = null;
        try{
            returnValue = future.get(100, TimeUnit.MILLISECONDS);
        }catch (TimeoutException e){
            future.cancel(true);
            throw e;
        }
        return returnValue;
    }

    private String doSay(String message) throws InterruptedException {
        int value = random.nextInt(200);
        System.out.println("say() costs " + value + "ms.");
        Thread.sleep(value);
        String returnValue = "Say："+message;
        System.out.println(returnValue);
        return returnValue;
    }
}
~~~

#### 5 高级版本（无注解实现）

~~~java
/**
 *  高级版本
 */
@RestController
@RequestMapping("/hystrix/advanced")
public class HystrixAdvancedController {

    private final static Random random = new Random();

    @GetMapping("/say")
    public String say(@RequestParam String message) throws Exception {
        return doSay(message);
    }

    private String doSay(String message) throws InterruptedException {
        int value = random.nextInt(200);
        System.out.println("say() costs " + value + "ms.");
        Thread.sleep(value);
        String returnValue = "Say："+message;
        System.out.println(returnValue);
        return returnValue;
    }

}
~~~

~~~java
/**
 * 高级版本的aop
 */
@Aspect
@Component
public class HystrixAdvancedControllerAspect {

    private ExecutorService executorService = Executors.newFixedThreadPool(20);

    @Around("execution(* com.wolfman.micro.hystrix.controller.HystrixAdvancedController.say(..)) && " +
            "args(message)")
    public Object advancedSayInTimeout(ProceedingJoinPoint point, String message) throws Throwable {
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
            returnValue = future.get(100, TimeUnit.MILLISECONDS);
        }catch (TimeoutException e){
            future.cancel(true);//取消执行
            returnValue = errorContent("");
            //throw e;
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
~~~

#### 6 高级版本 + 有注解实现

~~~java
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
~~~

~~~java
/**
 *  高级版本 + 注解
 */
@RestController
@RequestMapping("/hystrix/advanced/annotation")
public class HystrixAdvancedAnnotationController {

    private final static Random random = new Random();

    @GetMapping("/say")
    @CircuitBreaker(timeout = 100)
    public String say(@RequestParam String message) throws Exception {
        return doSay(message);
    }

    private String doSay(String message) throws InterruptedException {
        int value = random.nextInt(200);
        System.out.println("say() costs " + value + "ms.");
        Thread.sleep(value);
        String returnValue = "Say："+message;
        System.out.println(returnValue);
        return returnValue;
    }
}
~~~

~~~java
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
~~~

#### 7 高级版本 + 注解信号量方式

~~~java
@Target(ElementType.METHOD)//标注在方法
@Retention(RetentionPolicy.RUNTIME) //运行时保存注解信息
@Documented
public @interface SemaphoreCircuitBreaker {

    /**
     * 信号量
     * @return 设置超时时间
     */
    int value();

}
~~~

~~~java
/**
 *  高级版本 + 注解信号量
 */
@RestController
@RequestMapping("/hystrix/advanced/annotation/semaphore")
public class HystrixAdvancedAnnotationSemaphoreController {

    private final static Random random = new Random();

    /**
     * 高级版本 + 注解（信号量）
     * @param message
     * @return
     * @throws Exception
     */
    @GetMapping("/say")
    @SemaphoreCircuitBreaker(1)
    public String say(@RequestParam String message) throws Exception {
        return doSay(message);
    }

    private String doSay(String message) throws InterruptedException {
        int value = random.nextInt(200);
        System.out.println("say() costs " + value + "ms.");
        Thread.sleep(value);
        String returnValue = "Say："+message;
        System.out.println(returnValue);
        return returnValue;
    }

}
~~~

~~~java
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
~~~

