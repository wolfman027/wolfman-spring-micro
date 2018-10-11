## spring cloud gateway 服务网关

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

























