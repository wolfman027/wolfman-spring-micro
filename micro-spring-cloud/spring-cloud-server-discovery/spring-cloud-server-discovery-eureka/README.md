## spring cloud eureka 服务器发现

### 一 传统的服务发现

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