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

































