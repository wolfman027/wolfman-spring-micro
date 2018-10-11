## spring cloud feign 服务调用

### 一 申明式 web 服务客户端：feign - eureka

申明式：接口声明、Annotation 驱动

Web 服务：HTTP 的方式作为通讯协议

客户端：用于服务调用的存根

Feign：原生并不是 Spring Web MVC的实现，基于JAX-RS（Java REST 规范）实现。Spring Cloud 封装了Feign ，使其支持 Spring Web MVC。`RestTemplate`、`HttpMessageConverter`

\> `RestTemplate `以及 Spring Web MVC 可以显示地自定义 `HttpMessageConverter `实现。

假设，有一个java接口PersonService，Feign可以将其声明是以HTTP方式调用的。

![](https://github.com/wolfJava/wolfman-spring-micro/blob/master/spring-cloud-feign-eureka/img/feign-1.jpg?raw=true)

#### 需要服务组件（SOA Service Oriented Architecture：面向服务的架构）

- Feign 客户（服务消费）端
- Feign 服务（服务提供）端
- Feign 声明接口（契约） 存放在同一个工程目录。

#### 1 注册中心 — Eureka Server：服务发现和注册中心

代码见：https://github.com/wolfJava/wolfman-spring-micro/tree/master/spring-cloud-discovery-eureka/eureka-server

#### 2 Feign 声明接口（契约）：定义一种 java 强类型接口 — person-api

~~~java
@FeignClient(value = "person-service") // 服务提供方应用的名称
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
~~~

#### 3 Feign客户（服务消费）端：调用Feign申明接口 — person-client

~~~java
//controller
@RestController
public class PersonClientController implements PersonService {

    private final PersonService personService;

    @Autowired
    public PersonClientController(PersonService personService) {
        this.personService = personService;
    }

    @Override
    public boolean save(@RequestBody Person person) {
        return personService.save(person);
    }
    
    @Override
    public Collection<Person> findAll() {
        return personService.findAll();
    }
}
~~~

~~~java
//启动类
@SpringBootApplication
@EnableEurekaClient
@EnableFeignClients(clients = PersonService.class)
public class PersonClientApplication {
    public static void main(String[] args) {
        SpringApplication.run(PersonClientApplication.class, args);
    }
    @Bean
    public FirstServerForeverRule firstServerForeverRule() {
        return new FirstServerForeverRule();
    }
}
~~~

~~~properties
# 配置 application.properties
spring.application.name = person-client
server.port = 8080
# Eureka Server 服务URL，用于客户端注册
eureka.client.service-url.defaultZone=\
  http://localhost:12345/eureka
management.security.enabled = false
~~~

#### 4 Feign服务（服务提供）端：不一定强制实现Feign申明接口  — person-server

~~~java
//PersonServiceController
/**
 * {@link PersonService} 提供者控制器（可选实现{@link PersonService}）
 */
@RestController
public class PersonServiceProviderController {

    private final Map<Long, Person> persons = new ConcurrentHashMap<>();

    /**
     * 保存
     *
     * @param person {@link Person}
     * @return 如果成功，<code>true</code>
     */
    @PostMapping(value = "/person/save")
    public boolean savePerson(@RequestBody Person person) {
        return persons.put(person.getId(), person) == null;
    }

    /**
     * 查找所有的服务
     */
    @GetMapping(value = "/person/find/all")
    public Collection<Person> findAllPersons() {
        return persons.values();
    }
}
~~~

~~~java
// 创建服务端应用
@SpringBootApplication
@EnableEurekaClient
public class PersonServiceProviderApplication {

    public static void main(String[] args) {
        SpringApplication.run(PersonServiceProviderApplication.class,args);
    }

}
~~~

~~~properties
# 配置 application.properties
## 提供方的应用名称需要和 @FeignClient 声明对应
spring.application.name = person-service
## 提供方端口 9090
#server.port = ${random.int[9090,9099]}
server.port = 9090
## Eureka Server 服务 URL,用于客户端注册
eureka.client.service-url.defaultZone=\
  http://localhost:12345/eureka
## 关闭管理安全
management.security.enabled = false
~~~

#### 5 总结

调用顺序：PostMan -> person-client -> person-service

总结：

1. person-api 定义了 @FeignClients(value="person-service") , person-service 实际是一个服务器提供方的应用名称。
2. person-client 和 person-service 两个应用注册到了Eureka Server
3. person-client 可以感知 person-service 应用存在的，并且 Spring Cloud 帮助解析 `PersonService` 中声明的应用名称：“person-service”，因此 person-client 在调用 ``PersonService` `服务时，实际就路由到 person-service 的 URL

### 二 Feign Eureka 整合 Netflix Ribbon

官方参考文档：<http://cloud.spring.io/spring-cloud-static/Dalston.SR4/single/spring-cloud.html#spring-cloud-ribbon>

实现方式

#### 1 关闭 Eureka 注册

1. 调整 person-client 关闭 Eureka

~~~properties
# Ribbon 不使用Eureka
ribbon.eureka.enabled = false
~~~

2. 定义服务 ribbon 的服务列表（服务名称：person-service）

~~~properties
# 配置 person-service 的负载均衡服务器列表
person-service.ribbon.listOfServers = \
  http://localhost:9090,http://localhost:9090,http://localhost:9090
~~~

#### 2 完全取消 Eureka 注册

~~~java
//@EnableEurekaClient //注释 @EnableEurekaClient
~~~

#### 3 自定义 Ribbon 的规则

接口和 Netflix 内部实现

`IRule`

1. 随机规则：RandomRule
2. 最可用规则：BestAvailableRule
3. 轮训规则：RoundRobinRule
4. 重试实现：RetryRule
5. 客户端配置：ClientConfigEnabledRoundRobinRule
6. 可用性过滤规则：AvailabilityFilteringRule
7. RT权重规则：WeightedResponseTimeRule
8. 规避区域规则：ZoneAvoidanceRule

#### 4 实现 IRule

~~~java
/**
 * 自定义实现 {@link IRule}
 */
public class FirstServerForeverRule extends AbstractLoadBalancerRule {

    @Override
    public void initWithNiwsConfig(IClientConfig clientConfig) {
    }

    @Override
    public Server choose(Object key) {
        ILoadBalancer loadBalancer = getLoadBalancer();
        // 返回三个配置 Server，即：
        // person-service.ribbon.listOfServers = \
        // http://localhost:9090,http://localhost:9090,http://localhost:9090
        List<Server> allServers = loadBalancer.getAllServers();

        return allServers.get(0);
    }
}
~~~

~~~java
// 暴露自定义实现为 Spring Bean
@Bean
public FirstServerForeverRule firstServerForeverRule() {
    return new FirstServerForeverRule();
}
~~~

~~~java
/**
 * Person Client 应用程序
 * 暴露自定义实现为 Spring Bean
 * 激活这个配置
 */
@SpringBootApplication
@EnableFeignClients(clients = PersonService.class)
@RibbonClient(value = "person-service", configuration = PersonClientApplication.class)
public class PersonClientApplication {
 public static void main(String[] args) {
        SpringApplication.run(PersonClientApplication.class, args);
    }
    @Bean
    public FirstServerForeverRule firstServerForeverRule() {
        return new FirstServerForeverRule();
    }
}
~~~

#### 5 运行检测结果

~~~java
ILoadBalancer loadBalancer = getLoadBalancer();

// 返回三个配置 Server，即：
// person-service.ribbon.listOfServers = \
// http://localhost:9090,http://localhost:9090,http://localhost:9090
List<Server> allServers = loadBalancer.getAllServers();

return allServers.get(0);
~~~

### 三 Feign Eureka 整合 Netflix Hystrix

#### 1 调整 Feign 接口

~~~java
@FeignClient(value = "person-service",fallback = PersonServiceFallback.class) // 服务提供方应用的名称
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
~~~

#### 2 添加 Fallback 实现

~~~java
/**
 * {@link PersonService} Fallback 实现
 */
public class PersonServiceFallback implements PersonService {

    @Override
    public boolean save(Person person) {
        return false;
    }

    @Override
    public Collection<Person> findAll() {
        return Collections.emptyList();
    }
}
~~~

#### 3 调整客户端（激活Hystrix）

~~~java
@SpringBootApplication
@EnableEurekaClient
@EnableFeignClients(clients = PersonService.class)
@EnableHystrix
public class PersonClientApplication {
    public static void main(String[] args) {
        SpringApplication.run(PersonClientApplication.class, args);
    }
    @Bean
    public FirstServerForeverRule firstServerForeverRule() {
        return new FirstServerForeverRule();
    }
}
~~~

### 四 分析服务调用引入背景

@LoadBalanced RestTemplate 限制

- 面向 URL 组件，必须依赖于 主机 + 端口 + URI
- 并非接口编程（Spring Cloud 中，需要理解应用名称 + 服务 URI）

RestTemplate 不依赖于服务接口，仅关注 REST 响应内容。

**举例：**

> ```java
> restTemplate.getForObject("http://" + serviceName+"/say?message="+message,String.class);
> ```

### 五 Spring Cloud Feign 基本用法

#### 1 Spring Cloud Feign 客户端注解 @FeignClient

服务（应用）定位

> @FeignClient("${serviceName}")	// 服务提供方的应用名称

服务 URI 定位

> **注意：**Spring Cloud Feign 和 OpenFeign 区别

##### 1.1 服务端框架纵向比较

Spring Cloud Feign： 是 OpenFeign 扩展，并且使用 SpringMVC 注解来做 URI 映射，比如 @RequestMapping 或 @GetMapping 之类

OpenFeign ：灵感来自于 JAX-RS（Java REST 标准），重复发明轮子。

JAX-RS：Java REST 标准（https://github.com/mercyblitz/jsr/tree/master/REST），可一致性高，Jersey(Servlet容器)、Weblogic

> JSR参考链接：https://github.com/mercyblitz/jsr

- JAX-RS
  - HTTP 请求方法
- Spring Web MVC
- OpenFeign

| 技术栈             | HTTP 请求方法表达       | 变量路径      | 请求参数      | 自描述消息                                          | 内容协商 |
| ------------------ | ----------------------- | ------------- | ------------- | --------------------------------------------------- | -------- |
| JAX-RS             | @GET                    | @PathParam    | @FormParam    | @Produces("application/widgets+xml")                |          |
| Spring Web MVC     | @GetMapping             | @PathVariable | @RequestParam | @RequestMapping(produces="application/widgets+xml") |          |
| OpenFeign          | @RequestLine（GET ...） | @Param        | @Param        |                                                     |          |
| Spring Cloud Feign | @GetMapping             | @PathVariable | @RequestParam |                                                     |          |

##### 1.2 REST 核心概念（Java 技术描述）—— 非常重要

###### 请求映射（@RequestMapping）

自己补充内容

###### 请求参数处理（@RequestParam）

自己补充内容

###### 请求主体处理（@RequestBody）

自己补充内容

###### 响应处理（@ResponseBody，@ResponseStatus）

自己补充内容

@ResponseBody+@ResponseStatus<=@ResponseEntity

###### 自描述消息（@RequestMapping(produces="application/widgets+xml")）

自己补充内容

###### 内容协商（ContentNegotiationManager）

理论知识：https://developer.mozilla.org/en-US/docs/Web/HTTP/Content-negotiation

自己补充内容

#### 2 整合 Spring Cloud Feign — zk

##### 2.1 增加 Spring Cloud Feign 依赖

```java
<!-- 增加 feign 依赖 -->
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-openfeign</artifactId>
</dependency>
```

##### 2.2 整合 @EnableFeignClients

~~~java
@SpringBootApplication  //标准 Spring boot 应用
@EnableDiscoveryClient//激活服务发现客户端
@EnableFeignClients(clients = SayingService.class) //激活服务调用并引入FeignClient
public class FeignClientApplication {
    public static void main(String[] args) {
        new SpringApplicationBuilder(FeignClientApplication.class)
                .web(WebApplicationType.SERVLET)
                .run(args);
    }
}
~~~

##### 2.3 整合@FeignClient

~~~java
// 之前实现
@GetMapping("/loadBalance/invoke/{serviceName}/say")
public String lbInvokeSay(@PathVariable String serviceName,
                            @RequestParam String message){
	return lbRestTemplate.getForObject("http://" + serviceName+"/say?message="+message,String.class);
}
~~~

~~~java
// 整合 @FeignClient 实现
@FeignClient(name = "spring-cloud-server-application")
public interface SayingService {
    @GetMapping("/say")
    public String say(@RequestParam("message") String message);
}
~~~

~~~java
// 注入 SayingService
@Autowired
private SayingService sayingService;
~~~

~~~java
// 调用 SayingService
@GetMapping("/feign/say")
public String feignSay(@RequestParam String message){
    return sayingService.say(message);
}
~~~

启动 ZK 服务器

启动 spring-cloud-server-application

启动 spring-cloud-client-application

### 六 实现自定义 RestClient（模拟@FeignClient）

#### 1 Spring Cloud Feign 编程模型特征

- @Enable 模块驱动
- @*Client 绑定客户端接口，指定应用名称
- 客户端接口指定请求映射 @RequestMapping
- 客户端接口指定请求参数 @RequestParam
  - 必须指定 @RequestParamValue();
- @Autowired 客户端接口是一个代理

#### 2 实现 @FeignRestClient

















































