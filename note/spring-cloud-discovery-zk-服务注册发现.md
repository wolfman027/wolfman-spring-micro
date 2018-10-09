## spring cloud zk 服务注册发现

### 一 简单对比 Eureka

| 比较点     | Eureka                                                | zookeeper                    | Consul                     |
| ---------- | ----------------------------------------------------- | ---------------------------- | -------------------------- |
| 运维熟悉度 | 陌生                                                  | 熟悉                         | 更陌生                     |
| 一致性     | AP（最终一致性）                                      | CP（一直性强）               | AP（最终一致性）           |
| 一致性协议 | HTTP 定时轮询                                         | ZAB                          | RAFT                       |
| 通讯方式   | HTTP REST                                             | 自定义协议                   | HTTP REST                  |
| 更新机制   | Peer 2 Peer（服务器之间）+Scheduler（服务器和客户端） | ZK Watch                     | Agent 监听方式             |
| 适用规模   | 20k~30k 实例（节点）                                  | 10k~20k 实例（节点）         | <3k 实例（节点）           |
| 性能问题   | 简单的更新机制、复杂设计                              | 扩容麻烦、规模较大时，GC频繁 | 3k节点数以上，更新列表缓慢 |

### 二 为什么推荐使用 ZK 作为 Spring Cloud 的基础设施

- 一致性模型
- 维护相对熟悉
- 配置中心和服务注册中心单一化

#### 1 传统的问题

例如：Spring Cloud 默认配置、Eureka 做注册中心、Git/JDBC 做配置中心

### 三 Spring Cloud Discovery ZK 服务器

#### 1 Spring Cloud 增加 ZK 依赖

- 错误配置（高 ZK Client版本，低服务端版本3.4）

~~~java
<dependency>
	<groupId>org.springframework.boot</groupId>
	<artifactId>spring-boot-starter-web</artifactId>
</dependency>

<dependency>
	<groupId>org.springframework.cloud</groupId>
	<artifactId>spring-cloud-starter-zookeeper-discovery</artifactId>
</dependency>		
~~~

- 正确配置

~~~java
<dependency>
	<groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-zookeeper-all</artifactId>
    <exclusions>
    	<exclusion>
    		<groupId>org.apache.zookeeper</groupId>
    		<artifactId>zookeeper</artifactId>
    	</exclusion>
    </exclusions>
</dependency>

<dependency>
	<groupId>org.apache.zookeeper</groupId>
	<artifactId>zookeeper</artifactId>
	<version>3.4.10</version>
	<exclusions>
		<exclusion>
            <groupId>org.slf4j</groupId>
            <artifactId>slf4j-log4j12</artifactId>
        </exclusion>
	</exclusions>
</dependency>       
~~~

- 注册发现
- 配置管理

#### 2 启动 ZK（3.4.10）

#### 3 编写引导类

~~~java
@SpringBootApplication
@EnableDiscoveryClient //尽可能使用 @EnableDiscoveryClient
public class ZkDiscoveryClientAppliction {

    public static void main(String[] args) {
        SpringApplication.run(ZkDiscoveryClientAppliction.class,args);
    }
}

~~~

#### 4 在 bootstrap.properties 添加 zk 连接配置

~~~properties
spring.cloud.zookeeper.connect-string=39.107.31.208:2181
~~~

#### 5 启动两个服务

实例一：端口 54234

ZK ID：40a18ecf-7c78-43b7-945a-ce1488607eed

实例二：端口 54299

ZK ID：44521138-d34c-43d5-92c3-640d4096f9a5

ZK 节点路径（/services/spring-cloud-server-discovery-client）

ZK 服务发现节点规则（/services/${spring.application.name}/{serviceid}/）

~~~txt
{"name":"spring-cloud-server-discovery-client","id":"40a18ecf-7c78-43b7-945a-ce1488607eed","address":"wolfman","port":54234,"sslPort":null,"payload":{"@class":"org.springframework.cloud.zookeeper.discovery.ZookeeperInstance","id":"application-1","name":"spring-cloud-server-discovery-client","metadata":{}},"registrationTimeUTC":1533737471911,"serviceType":"DYNAMIC","uriSpec":{"parts":[{"value":"scheme","variable":true},{"value":"://","variable":false},{"value":"address","variable":true},{"value":":","variable":false},{"value":"port","variable":true}]}}

~~~

**Eureka 2.0 不开源，Eureka 1.x 还可以用的**