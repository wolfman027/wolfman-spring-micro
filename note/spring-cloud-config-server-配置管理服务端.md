## Spring Cloud Server

### 一 构建 Spring Cloud 配置服务器

实现步骤：

1. 在 Configuration Class 标记`@EnableConfigServer`

2. 配置文件目录（基于 git）

3. 1. gupao.properties （默认） // 默认环境，跟着代码仓库
   2. gupao-dev.properties ( profile = "dev") // 开发环境
   3. gupao-test.properties ( profile = "test") // 测试环境
   4. gupao-staging.properties ( profile = "staging") // 预发布环境
   5. gupao-prod.properties ( profile =  "prod") // 生产环境

3. 服务端配置配置版本仓库（本地）

~~~properties
spring.cloud.config.server.git.uri = \
     file:///D:/咕泡例子/xiaomage/git/spring-cloud/lession2/config
## 注意：.properties文件放在存有`.git`的根目录     
~~~

完整的配置项：

~~~properties
##配置服务器配置项
spring.application.name=cloud-server

##服务端端口
server.port=9090

## 本地残酷的GIT URI 配置
spring.cloud.config.server.git.uri = \
  file:///d:/gupao-example/xiaomage/git/spring-cloud

##全局关闭 Actuator 安全
##management.security.enabled=false
##细粒度的开放Endpoints
##sensitive 关注是敏感性，安全
endpoints.env.sensitive=false
##2.0版本之后配置有所改变，此配置为1.*版本配置
~~~

### 二 构建 Spring Cloud 配置客户端

实现步骤：

1. 创建`bootstrap.properties` 或者 `bootstrap.yml`文件
2. `bootstrap.properties` 或者 `bootstrap.yml`文件中配置客户端信息

~~~properties
### bootstrap 上下文配置
#配置服务器URI
spring.cloud.config.uri=http://localhost:9090/
#配置客户端应用名称：{application}
spring.cloud.config.name = gupao
#profile指的是 配置gupao-dev.properties
spring.cloud.config.profile = dev
#label 在Git中指的分支名称
spring.cloud.config.label = master
# uri = http://localhost:9090/
#{application} = gupao
#{profile} = dev
#{label} = master
#URL:{uri}/{application}-{profile}.properties = http://localhost:9090/gupao-dev.properties
~~~

3. 在applicationg.properties设置关键 Endpoints 的敏感性

~~~properties
## 配置客户端配置项
spring.application.name=cloud-client
### 全局关闭 Actuator 安全
management.security.enabled = false
### 细粒度的开放 Actuator Endpoints
### sensitive 关注是敏感性，安全
endpoints.env.sensitive = false
endpoints.refresh.sensitive = false
endpoints.beans.sensitive = false
endpoints.health.sensitive = false
endpoints.actuator.sensitive = false
##2.0版本之后配置有所改变，此配置为1.*版本配置
~~~

ls -als：git命令中，查询文件夹中所有的文件

<http://localhost:8080/env> 环境地址

localhost:8080/refresh 刷新properties配置信息

### 三 动态配置属性 Bean

#### 1 @RefreshScope 用法

~~~java
@RestController
@RefreshScope
public class EchoController {

    @Value("${my.name}")
    private String myName;

    @GetMapping("/my-name")
    public String getName(){
        return myName;
    }
}
~~~

#### 2 通过调用`/refresh` Endpoint 控制客户端配置更新

#### 3 实现定时更新客户端

~~~java
private final ContextRefresher contextRefresher;

private final Environment environment;

@Autowired
public SpringCloudConfigClientDemoApplication(ContextRefresher contextRefresher, Environment environment) {
   this.contextRefresher = contextRefresher;
   this.environment = environment;
}

@Scheduled(fixedRate = 5 * 1000, initialDelay = 3 * 1000)
public void autoRefresh() {
   Set<String> updatedPropertyNames = contextRefresher.refresh();
   updatedPropertyNames.forEach( propertyName ->
         System.err.printf("[Thread :%s] 当前配置已更新，具体 Key：%s , Value : %s \n",
               Thread.currentThread().getName(),
               propertyName,
               environment.getProperty(propertyName)
         ));
}
~~~

### 四 健康指标

#### 1 健康检查

1. 意义：比如应用可以任意地输出业务健康、系统健康等指标
2. 端点URI：`/health`
3. 实现类：`HealthEndpoint`
4. 健康指示器：`HealthIndicator`，
5. `HealthEndpoint`：`HealthIndicator` ，一对多

#### 2 自定义实现`HealthIndicator`

1. 实现`AbstractHealthIndicator`

~~~java
public class MyHealthIndicator extends AbstractHealthIndicator {
    @Override
    protected void doHealthCheck(Health.Builder builder) throws Exception {
        builder.up().withDetail("MyHealthIndicator", "Day Day Up");
    }
}
~~~

2. 暴露 `MyHealthIndicator` 为 `Bean`

~~~java
@Bean
public MyHealthIndicator myHealthIndicator(){
   return new MyHealthIndicator();
}
~~~

3. 关闭安全控制

~~~properties
management.security.enabled = false
~~~

#### 3 Spring Boot 激活 `actuator` 需要增加 Hateoas 的依赖

~~~xml
<dependency>
   <groupId>org.springframework.hateoas</groupId>
   <artifactId>spring-hateoas</artifactId>
</dependency>
~~~

以客户端为例：

~~~java
http://localhost:8080/actuator
{
    "links": [{
        "rel": "self",
        "href": "http://localhost:8080/actuator"
    }, {
        "rel": "heapdump",
        "href": "http://localhost:8080/heapdump"
    }, {
        "rel": "beans",
        "href": "http://localhost:8080/beans"
    }, {
        "rel": "resume",
        "href": "http://localhost:8080/resume"
    }, {
        "rel": "autoconfig",
        "href": "http://localhost:8080/autoconfig"
    }, {
        "rel": "refresh",
        "href": "http://localhost:8080/refresh"
    }, {
        "rel": "env",
        "href": "http://localhost:8080/env"
    }, {
        "rel": "auditevents",
        "href": "http://localhost:8080/auditevents"
    }, {
        "rel": "mappings",
        "href": "http://localhost:8080/mappings"
    }, {
        "rel": "info",
        "href": "http://localhost:8080/info"
    }, {
        "rel": "dump",
        "href": "http://localhost:8080/dump"
    }, {
        "rel": "loggers",
        "href": "http://localhost:8080/loggers"
    }, {
        "rel": "restart",
        "href": "http://localhost:8080/restart"
    }, {
        "rel": "metrics",
        "href": "http://localhost:8080/metrics"
    }, {
        "rel": "health",
        "href": "http://localhost:8080/health"
    }, {
        "rel": "configprops",
        "href": "http://localhost:8080/configprops"
    }, {
        "rel": "pause",
        "href": "http://localhost:8080/pause"
    }, {
        "rel": "features",
        "href": "http://localhost:8080/features"
    }, {
        "rel": "trace",
        "href": "http://localhost:8080/trace"
    }]
}
~~~





















