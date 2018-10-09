## spring cloud eureka 服务器发现

### 一 传统的服务治理

#### 1 分布式系统的基本组成

1. 服务提供方（Provider）
2. 服务消费方（Consumer）
3. 服务注册中心（Registry）
4. 服务路由（Router）
5. 服务代理（Broker）
6. 通讯协议（Protocol）

#### 2 通讯协议

1. XML-RPC -> XML 方法描述、方法参数 -> WSDL（WebServices 定义语言）

2. WebServices -> SOAP（HTTP、SMTP） -> 文本协议（头部分、体部分）

3. REST -> JSON/XML( Schema ：类型、结构) -> 文本协议（HTTP Header、Body）

4. 1. W3C Schema ：xsd:string 原子类型，自定义自由组合原子类型
   2. Java POJO : int、String
   3. Response Header -> Content-Type: application/json;charset=UTF-8

5. Dubbo：Hession、 Java Serialization（二进制），跨语言不变，一般通过 Client（Java、C++）

6. 1. 二进制的性能是非常好（字节流，免去字符流（字符编码），免去了字符解释，机器友好、对人不友好）
   2. 序列化：把编程语言数据结构转换成字节流、反序列化：字节流转换成编程语言的数据结构（原生类型的组合）

### 二 高可用架构

#### 1 基本原理

- 消除单点失败
- 可靠性交迭
- 故障探测

URI：统一资源定位符

http://git.gupaoedu.com/vip/xiaomage-space/tree/master/VIP课/spring-cloud/lesson-3

URI：用于网络资源定位的描述 Universal Resource Identifier

URL: Universal Resource Locator

网络是通讯方式

资源是需要消费媒介

定位是路由

Proxy：一般性代理，路由

​	Nginx：反向代理	

Broker：包括路由，并且管理，老的称谓（MOM）

​	Message Broker：消息路由、消息管理（消息是否可达）

#### 2 可用性比率计算

可用性比率：通过时间来计算（一年或者一月）

比如：一年 99.99 % 

可用时间：365 * 24  * 3600 * 99.99% 

不可用时间：365 * 24  * 3600 * 0.01% = 3153.6 秒 < 一个小时

不可以时间：1个小时 推算一年 1 / 24 / 365 = 0.01 %

单台机器不可用比率：1%

两台机器不可用比率：1% * 1%

N 机器不可用比率：1% ^ n

#### 3 可靠性

微服务里面的问题：

一次调用：

   A ->       B    ->  C

99% -> 99% -> 99% = 97%

   A ->     B    ->  C -> D

99% -> 99% -> 99%  -> 99% = 96%

结论：增加机器可以提高可用性，增加服务调用会降低可靠性，同时降低了可用性

### 三 Eureka 注册发现服务

#### 1 Eureka 服务器

- Eureka 服务器一般不需要自我注册，也不需要注册其他服务器
- Eureka  自我注册的问题，服务器本身没有启动

Fast Fail : 快速失败

Fault-Tolerance ：容错

通常经验，Eureka 服务器不需要开启自动注册，也不需要检索服务

~~~java
# 取消服务器自我注册
eureka.client.register-with-eureka=false
# 注册中心的服务器，没有必要再去检索服务
eureka.client.fetch-registry = false
~~~

但是这两个设置并不是影响作为服务器的使用，不过建议关闭，为了减少不必要的异常堆栈，减少错误的干扰（比如：系统异常和业务异常）

![](https://github.com/wolfJava/wolfman-spring-micro/blob/master/spring-cloud-discovery-eureka/img/eureka1.jpg?raw=true)

#### 2 Eureka 实例

- 创建eureka服务端 https://github.com/wolfJava/wolfman-spring-micro/tree/master/spring-cloud-discovery-eureka/eureka-server
- 创建eureka客户端 https://github.com/wolfJava/wolfman-spring-micro/tree/master/spring-cloud-discovery-eureka/eureka-client

### 四 Eureka 的客户端高可用

#### 1 高可用注册中心集群

~~~properties
# 只需要增加 Ereka 服务器注册地址URL
# Eureka Server 服务 URL,用户客户端注册
eureka.client.service-url.defaultZone=\
  http://localhost:9090/eureka,http://localhost:9091/eureka
~~~

如果 Eureka 客户端应用配置多个 Eureka 注册服务器，那么默认情况只有第一台可用的服务器，存在信息。

如果第一台可用的 Eureka 服务器Down掉了，那么Eureka客户端应用将会选择下一台可用的Eureka服务器。

#### 2 配置源码（EurekaClientConfigBean）

配置项 `eureka.client.serviceUrl` 实际映射的字段为 `serviceUrl` ，他是Map类型，key为自定义，默认值“defaultZone”，value 是需要配置的Eureka 注册服务器URL。

~~~java
private Map<String, String> serviceUrl = new HashMap<>();{
   this.serviceUrl.put(DEFAULT_ZONE, DEFAULT_URL);
}
~~~

value 可以是多值字段，通过“,” 分割：

~~~java
String serviceUrls = this.serviceUrl.get(myZone);
if (serviceUrls == null || serviceUrls.isEmpty()) {
   serviceUrls = this.serviceUrl.get(DEFAULT_ZONE);
}
if (!StringUtils.isEmpty(serviceUrls)) {
   final String[] serviceUrlsSplit = 
         StringUtils.commaDelimitedListToStringArray(serviceUrls);
}
~~~

#### 3 获取注册信息时间间隔

Eureka客户端需要获取Eurekal服务器注册信息，这个方便服务调用。

Eureka客户端：EurekaClient，关联应用集合：Applications

单个应用信息：Application，关联多个应用实例

单个应用实例：InstanceInfo

当Eureka客户端需要调用具体某个服务时，比如比如`user-service-consumer` 调用`user-service-provider`，`user-service-provider`实际对应对象是`Application`,关联了许多应用实例(`InstanceInfo`)。

如果应用`user-service-provider`的应用实例发生变化时，那么`user-service-consumer`是需要感知的。比如：`user-service-provider`机器从10 台降到了5台，那么，作为调用方的`user-service-consumer`需要知道这个变化情况。可是这个变化过程，可能存在一定的延迟，可以通过调整注册信息时间间隔来减少错误。

~~~properties
# 调整注册信息获取周期，默认值：30秒
eureka.client.registry-fetch-interval-seconds=5
~~~

#### 4 实例信息赋值时间间隔

具体就是客户端信息的上报到Eureka服务器时间，当 Eureka 客户端应用上报的频率越频繁，那么 Eureka 服务器的应用状态管理一致性就越高。

~~~properties
# 调整客户端应用状态信息上报的周期，默认值：30秒
eureka.client.instance-info-replication-interval-seconds=5
~~~

Eureka的应用信息获取的方式：拉模式

Eureka的应用信息上报的方式：推模式

#### 5 实例id

从 Eureka Server Dashboard 里面可以看到具体某个应用中的实例信息，比如：

~~~html
UP (2) -localhost:user-service-provider:7074,localhost:user-service-provider:7070
~~~

其中，他们的命名模式：`${hostname}:${spring.application.name}:${server.port}`

实例类：`EurekaInstanceConfigBean`

~~~properties
# Eureka实例应用的ID
eureka.instance.instance-id=${spring.application.name}:${server.port}
~~~

#### 5 实例端点映射

源码位置：`EurekaInstanceConfigBean`

~~~java
private String statusPageUrlPath = "/info";
~~~

配置项：

~~~properties
# Eureka客户端应用实例状态URL
eureka.instance.status-page-url-path=/health
~~~

### 五 Eureka服务端高可用配置

#### 1 构建 Eureka 服务器相互注册

Eureka Server 1 -> Profile : peer1

~~~properties
# Eureka Server 应用名称
spring.application.name = spring-cloud-eureka-server
# Eureka Server 端口号
server.port= 9090
# 取消服务器自我注册
eureka.client.register-with-eureka=true
# 注册中心的服务器，没有必要再去检索服务
eureka.client.fetch-registry=true

# Eureka Server 服务 URL
# 当前Eureka服务器 向 9091（Eureka服务器）复制数据
eureka.client.service-url.defaultZone=\
  http://localhost:9091/eureka
~~~

Eureka Server 2 -> Profile : peer2

~~~properties
# Eureka Server 应用名称
spring.application.name = spring-cloud-eureka-server
# Eureka Server 端口号
server.port= 9091
# 取消服务器自我注册
eureka.client.register-with-eureka=true
# 注册中心的服务器，没有必要再去检索服务
eureka.client.fetch-registry=true
# Eureka Server 服务 URL
# 当前Eureka服务器 向 9090（Eureka服务器）复制数据
eureka.client.service-url.defaultZone=\
  http://localhost:9090/eureka
~~~

通过`--spring.profiles.active=peer1` 和 `--spring.profiles.active=peer2` 分别激活 Eureka Server 1 和 Eureka Server 2

Eureka Server 1 里面的replicas 信息：

| registered-replicas | http://localhost:9091/eureka/ |

Eureka Server 2 里面的replicas 信息：

| registered-replicas | http://localhost:9090/eureka/ | 



