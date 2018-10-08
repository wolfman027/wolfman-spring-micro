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

![](https://github.com/wolfJava/wolfman-spring-micro/blob/master/micro-spring-cloud/spring-cloud-server-discovery/spring-cloud-server-discovery-eureka/img/eureka1.jpg?raw=true)

#### 2 Eureka 实例

























