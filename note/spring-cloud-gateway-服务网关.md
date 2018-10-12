## spring cloud gateway 服务网关

![](https://github.com/wolfJava/wolfman-spring-micro/blob/master/spring-cloud-gateway/img/zuul-1.jpg?raw=true)

### 一 Zuul 基本使用 — eureka

#### 1 Zuul 基本使用

@EnableEurekaClient

@EnableDiscoveryClient

Nginx + Lua 

Lua：控制规则（A/B Test）

Spring Cloud 学习技巧：

善于定位应用：Feign、Config Server、Eureka、Zuul 、Ribbon定位应用，配置方式是不同

##### 1.1 增加 @EnableZuulProxy

~~~java
@SpringBootApplication
@EnableZuulProxy
public class SpringCloudZuulDemoApplication {
   public static void main(String[] args) {
      SpringApplication.run(SpringCloudZuulDemoApplication.class, args);
   }
}
~~~

##### 1.2 配置路由规则

~~~properties
// 基本模式
## Zuul 基本配置模式
# zuul.routes.${app-name}: /${app-url-prefix}/**
## Zuul 配置 person-service 服务调用
zuul.routes.person-service = /person-service/**
## Zuul 配置 person-client 服务调用
zuul.routes.person-client = /person-client/**
~~~

测试请求：

http://localhost:7070/person-client/person/find/all

http://localhost:7070/person-service/person/find/all

#### 2 Zuul 整合 Ribbon

- 调用链路

zuul ->  person-service

- 启动应用

spring-cloud-eureka-server

person-service

- 配置方式

~~~properties
# Zuul 服务端口
server.port=7070

# Zuul 基本配置模式
#zuul.routes.${app-name} = /${app-url-prefix}/**
# Zuul 配置 person-service 服务调用
zuul.routes.person-service = /person-service/**
 
 ## Ribbon 取消 Eureka 整合
ribbon.eureka.enabled = false
## 配置 "person-service" 的负载均衡服务器列表
person-service.ribbon.listOfServers = \
  http://localhost:9090
## 配置 "person-client" 的负载均衡服务器列表
person-client.ribbon.listOfServers = \
  http://localhost:8080
~~~

> 注意：http://localhost:7070/person-service/person/find/all
>
> person-service 的 app-url-prefix : /person-service/ 
>
> /person/find/all 是 person-service 具体的 URI

#### 3 Zuul 整合 Eureka

- 引入 spring-cloud-starter-eureka 依赖

~~~xml
<!-- 增加 Eureka 客户端的依赖 -->
<dependency>
   <groupId>org.springframework.cloud</groupId>
   <artifactId>spring-cloud-starter-eureka</artifactId>
</dependency>
~~~

- 激活服务注册、发现客户端

~~~java
@SpringBootApplication
@EnableZuulProxy
@EnableDiscoveryClient
public class SpringCloudZuulDemoApplication {
   public static void main(String[] args) {
      SpringApplication.run(SpringCloudZuulDemoApplication.class, args);
   }
}
~~~

- 配置服务注册、发现客户端

~~~properties
# 整合 Eureka
# 目标应用的serviceId = person-service
# Eureka Server 服务 URL,用于客户端注册
eureka.client.service-url.defaultZone=\
  http://localhost:12345/eureka
~~~

#### 4 Zuul 整合 Hystrix

在服务端提供方进行配置：zuul-person-service-provider

- 激活 Hystrix

~~~java
@SpringBootApplication
@EnableEurekaClient
@EnableHystrix
public class PersonServiceProviderApplication {

    public static void main(String[] args) {
        SpringApplication.run(PersonServiceProviderApplication.class,args);
    }

}
~~~

- 配置 Hystrix 规则

~~~java
@RestController
public class PersonServiceProviderController {

    private final Map<Long, Person> persons = new ConcurrentHashMap<>();

    private final static Random random = new Random();

    /**
     * 保存
     */
    @PostMapping(value = "/person/save")
    public boolean savePerson(@RequestBody Person person) {
        return persons.put(person.getId(), person) == null;
    }

    /**
     * 查找所有的服务
     */
    @GetMapping(value = "/person/find/all")
    @HystrixCommand(fallbackMethod = "fallbackForFindAllPersons",
            commandProperties = {
                    @HystrixProperty(name = "execution.isolation.thread.timeoutInMilliseconds",
                            value = "100")
            }
    )
    public Collection<Person> findAllPersons() throws Exception {
        // 如果随机时间 大于 100 ，那么触发容错
        int value = random.nextInt(200);
        Thread.sleep(value);
        System.out.println("findAllPersons() costs " + value + " ms.");
        return persons.values();
    }

    /**
     * {@link #findAllPersons()} Fallback 方法
     * @return 返回空集合
     */
    public Collection<Person> fallbackForFindAllPersons() {
        System.err.println("fallbackForFindAllPersons() is invoked!");
        return Collections.emptyList();
    }
}
~~~

#### 5 Zuul 整合 Feign

##### 5.1 服务消费端 zuul-person-client

调用链路：spring-cloud-zuul -> person-client -> person-service

- person-client 注册到 EurekaServer

端口信息：

1. spring-cloud-zuul 端口：7070
2. person-client 端口：7060
3. person-service 端口：7071
4. Eureka Server 端口：7777

- 启动客户端

zuul-person-client

~~~properties
spring.application.name = person-client
server.port = 7060
## Eureka Server 服务 URL
eureka.client.serviceUrl.defaultZone=\
  http://localhost:12345/eureka
management.security.enabled = false
~~~

##### 5.2 网关应用：spring-cloud-zuul

增加路由应用到 person-client

~~~properties
# Zuul 配置 person-client 服务调用
zuul.routes.person-client = /person-client/**
~~~

- 测试链路

<http://localhost:7070/person-client/person/find/all>

spring-cloud-zuul(**7070**) -> person-client(**7060**) -> person-service(**7071**)

等价的 Ribbon（不走注册中心）

~~~properties
## Ribbon 取消 eureka 整合
ribbon.eureka.enabled = false
## 配置 "person-service" 的负载均衡服务器列表
person-service.ribbon.listOfServers = \
# http://localhost:9090
## 配置 "person-client" 的负载均衡服务器列表
person-client.ribbon.listOfServers = \
  http://localhost:8080
~~~

#### 6 Zuul 整合 Config Server

动态路由，即需要动态配置。

##### 6.1 配置服务器：spring-cloud-config-server

端口信息：

1. spring-cloud-zuul 端口：7070
2. person-client 端口：7060
3. person-service 端口：7071
4. Eureka Server 端口：7777
5. Config Server 端口：10000

- 调整spring-cloud-config-server配置项

~~~properties
# 配置服务器配置项
spring.application.name=spring-cloud-config-server

# 服务端端口
server.port=10000

# 本地残酷的GIT URI 配置
# spring.cloud.config.server.git.uri = file:///d:/gupao-example/xiaomage/git/spring-cloud
spring.cloud.config.server.git.uri = file:///${user.dir}/src/main/resources/configs

# 全局关闭 Actuator 安全
# management.security.enabled=false
# 细粒度的开放Endpoints
# sensitive 关注是敏感性，安全
endpoints.env.sensitive=false
endpoints.health.sensitive=false
~~~

- 为 spring-cloud-zuul 增加配置文件

三个 profile 的配置文件：

~~~properties
# zuul.properties
# 实际的应用 spring-cloud-zuul 默认配置项（profile 为空）

# Zuul 配置 person-service 服务调用
zuul.routes.person-service = /person-service/**
~~~

~~~properties
# zuul-test.properties
# 实际的应用 spring-cloud-zuul 默认配置项（profile == "test" 为空）

# Zuul 配置 person-client 服务调用
zuul.routes.person-client = /person-client/**
~~~

~~~properties
# zuul-prod.properties
# 实际的应用 spring-cloud-zuul 默认配置项（profile =="prod" 为空）
# Zuul 配置 person-service 服务调用
zuul.routes.person-service = /person-service/**
# Zuul 配置 person-client 服务调用
zuul.routes.person-client = /person-client/**
~~~

- 初始化 ${user.dir}/src/main/resources/configs 为 git 根目录

1. 初始化

~~~java
git init
Initialized empty Git repository in D:/gupao-example/xiaomage/git/spring-cloud/spring-cloud/lession2/spring-cloud-config-server-demo/src/main/resources/configs/.git/
~~~

2. 增加上述三个配置文件到 git 仓库

$ git add *.properties

3. 提交到本地 git 仓库

$ git commit -m "Temp commit"

以上操作为了让 Spring Cloud Git 配置服务器实现识别 Git 仓库，否则添加以上三个文件也没有效果。

- 注册到 Eureka 服务器

1. 增加 spring-cloud-starter-eureka 依赖

~~~xml
<dependency>
   <groupId>org.springframework.cloud</groupId>
   <artifactId>spring-cloud-starter-eureka</artifactId>
</dependency>
~~~

2. 激活服务注册、发现客户端

~~~java
@SpringBootApplication
@EnableConfigServer
@EnableDiscoveryClient
public class SpringCloudConfigServerDemoApplication {
   public static void main(String[] args) {
      SpringApplication.run(SpringCloudConfigServerDemoApplication.class, args);
   }
}
~~~

3. 调整配置项

~~~properties
# Eureka Server 服务URL，用于客户端注册
eureka.client.serviceUrl.defaultZone=\
  http://localhost:7070/eureka
~~~

4. 测试配置

> http://localhost:10000/zuul/default
> http://localhost:10000/zuul/test
> http://localhost:10000/zuul/prod

##### 6.2 配置网关服务：spring-cloud-zuul

端口信息：
​	spring-cloud-zuul 端口：7070
​	person-client 端口：7060
​	person-service 端口：7071
​	Eureka Server 端口：7777

- 增加 spring-cloud-starter-config 依赖

1. 将之前的配置注释

2. 1. zuul.routes.person-service
   2. zuul.routes.person-client

2. 增加客户端依赖

~~~xml
<!-- 增加配置客户端的依赖 -->
<dependency>
   <groupId>org.springframework.cloud</groupId>
   <artifactId>spring-cloud-starter-config</artifactId>
</dependency>
~~~

3. 创建 bootstrap.properties、配置 config 客户端信息

~~~properties
### bootstrap 上下文配置
#配置客户端应用名称：{application}
spring.cloud.config.name = zuul
#profile指的是 配置gupao-dev.properties
spring.cloud.config.profile = prod
#label 在Git中指的分支名称
spring.cloud.config.label = master

# 采用 Discovery client 配置方式
# 激活 discovery 连接配置项的方式
spring.cloud.config.discovery.enabled=true
# 配置 config server 应用名称
spring.cloud.config.discovery.service-id=spring-cloud-config-server

## 整合 Eureka
## Eureka Server 服务 URL,用于客户端注册
## application.properties 会继承bootstrap.properties 属性
## 因此，application.properties 没有必要配置 eureka.client.serviceUrl.defaultZone
eureka.client.serviceUrl.defaultZone=\
  http://localhost:12345/eureka
~~~

- 测试链路

> 1. <http://localhost:7070/person-client/person/find/all>
> 2. [spring-cloud-zuul -> person-client -> person-service](http://localhost:7070/person-client/person/find/all)
> 3. <http://localhost:7070/person-service/person/find/all>
> 4. [spring-cloud-zuul -> person-service](http://localhost:7070/person-service/person/find/all)

### 二 Spring Cloud Gateway

![](https://github.com/wolfJava/wolfman-spring-micro/blob/master/spring-cloud-gateway/img/zuul-2.jpg?raw=true)

Spring WebFlux 相像

目的：去 Servlet 化（Java EE Web 技术中心）

技术：Reactor + Netty + Lambda

最新技术：Spring Cloud Function

#### 1 取代 Zuul 1.x（基于Servlet）

- Resin Servlet 
  - 可以与 Nginx 匹敌
- Tomcat Servlet 容器
  - 连接器
    - Java Blocking Connector
    - Java Non Blocking Connector
    - APR/native Connector
- JBoss
- Weblogic



- Netflix Zuul 自己的实现
  - 实现 API 不是非常友好

#### 2 Zuul 实现原理

- @Enable 模块装配
  - @EnableZuulProxy
  - 配合注解：@Import
- 依赖服务发现
  - 我是谁
  - 目的地在哪里
- 依赖服务路由
  - URI映射到目的的服务
- 依赖服务熔断（可选）

##### 2.1 服务发现

**举例说明：**

假设 URI：/gateway/spring-cloud-server-application/say

其中 Servlet Path：/gateway

spring-cloud-server-application 是服务器的应用名称

/say是 spring-cloud-server-application 的服务 URI

/gateway/spring-cloud-server-application/say -> http://${rest-api-host}:${rest-api-port}/hello-world?...

#####  2.2 根据 servlet 实现网关

~~~java
/**
 * 服务网关路由规则
 *
 * /${service-name}/${service-uri}
 * /gateway/rest-api/hello-world -> http://127.0.0.1:9090/hello-world
 *
 */
@WebServlet(name = "gateway", urlPatterns = "/gateway/*")
public class GatewayServlet extends HttpServlet {

    @Autowired
    private DiscoveryClient discoveryClient;

    private ServiceInstance randomChooseServiceInstance(String serviceName){
        //获取服务实例列表（服务IP ，端口，是否为HTTPS）
        List<ServiceInstance> serviceInstances = discoveryClient.getInstances(serviceName);
        //获得服务实例总数
        int size = serviceInstances.size();
        //随机获取数组下标
        int index = new Random().nextInt(size);
        return serviceInstances.get(index);
    }


    @Override
    public void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        //${service-name}/${service-uri}
        String pathInfo = request.getPathInfo();

        String[] paths = StringUtils.split(pathInfo.substring(1),"/");
        //获取服务名称
        String serviceName = paths[0];
        //获取服务Uri
        String serviceUri = "/" + paths[1];
        //随机选择一台服务实例
        ServiceInstance serviceInstance = randomChooseServiceInstance(serviceName);
        //构建目标服务 URI -> scheme://ip:port/serviceURI
        String targetURL = buildTargetURI(serviceInstance,serviceUri,request);

        // 创建转发客户端
        RestTemplate restTemplate = new RestTemplate();

        // 构造 Request 实体
        RequestEntity<byte[]> requestEntity = null;
        try {
            requestEntity = createRequestEntity(request, targetURL);
            ResponseEntity<byte[]> responseEntity = restTemplate.exchange(requestEntity, byte[].class);
            writeHeaders(responseEntity, response);
            writeBody(responseEntity, response);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }


    }

    private String buildTargetURI(ServiceInstance serviceInstance, String serviceURI, HttpServletRequest request){
        StringBuilder urlBuilder = new StringBuilder();
        urlBuilder.append(serviceInstance.isSecure() ? "https://":"http://")
                .append(serviceInstance.getHost())
                .append(":").append(serviceInstance.getPort())
                .append(serviceURI);
        String queryString = request.getQueryString();
        if (StringUtils.hasText(queryString)){
            urlBuilder.append("?").append(queryString);
        }
        return urlBuilder.toString();
    }

    private RequestEntity<byte[]> createRequestEntity(HttpServletRequest request, String url) throws URISyntaxException, IOException {
        // 获取当前请求方法
        String method = request.getMethod();
        // 装换 HttpMethod
        HttpMethod httpMethod = HttpMethod.resolve(method);
        byte[] body = createRequestBody(request);
        MultiValueMap<String, String> headers = createRequestHeaders(request);
        RequestEntity<byte[]> requestEntity = new RequestEntity<byte[]>(body, headers, httpMethod, new URI(url));
        return requestEntity;
    }

    private MultiValueMap<String, String> createRequestHeaders(HttpServletRequest request) {
        HttpHeaders headers = new HttpHeaders();
        List<String> headerNames = Collections.list(request.getHeaderNames());
        for (String headerName : headerNames) {
            List<String> headerValues = Collections.list(request.getHeaders(headerName));
            for (String headerValue : headerValues) {
                headers.add(headerName, headerValue);
            }
        }
        return headers;
    }

    private byte[] createRequestBody(HttpServletRequest request) throws IOException {
        InputStream inputStream = request.getInputStream();
        return StreamUtils.copyToByteArray(inputStream);
    }


    /**
     * 输出 Body 部分
     *
     * @param responseEntity
     * @param response
     * @throws IOException
     */
    private void writeBody(ResponseEntity<byte[]> responseEntity, HttpServletResponse response) throws IOException {
        if (responseEntity.hasBody()) {
            byte[] body = responseEntity.getBody();
            // 输出二进值
            ServletOutputStream outputStream = response.getOutputStream();
            // 输出 ServletOutputStream
            outputStream.write(body);
            outputStream.flush();
        }
    }

    private void writeHeaders(ResponseEntity<byte[]> responseEntity, HttpServletResponse response) {
        // 获取相应头
        HttpHeaders httpHeaders = responseEntity.getHeaders();
        // 输出转发 Response 头
        for (Map.Entry<String, List<String>> entry : httpHeaders.entrySet()) {
            String headerName = entry.getKey();
            List<String> headerValues = entry.getValue();
            for (String headerValue : headerValues) {
                response.addHeader(headerName, headerValue);
            }
        }
    }
}
~~~



#### 3 整合负载均衡（Ribbon）

官方实现：看文档

http://cloud.spring.io/spring-cloud-static/Finchley.SR1/single/spring-cloud.html#_ribbon_with_zookeeper

#### 4 实现 ILoadBalancer

#### 5 实现 IRule











