## Spring-cloud-config-client

### 一 Spring 和 Spring Boot 事件机制

#### 1 发布/订阅模式

1. `java.util.Observable` 是一个发布者
2. `java.util.Observer` 是订阅者
3. 发布者和订阅者：1 : N，发布者和订阅者：N : M

**demo详见项目中代码**

#### 2 事件/监听模式

1. `java.util.EventObject` ：事件对象

2. 1. 事件对象总是关联着事件源（source）

3. `java.util.EventListener` ：事件监听接口（标记）

#### 3 Spring 事件/监听

1. `ApplicationEvent` : 应用事件
2. `ApplicationListener` : 应用监听器

**demo详见项目中代码**

### 二 spring boot 事件/监听

1. ConfigFileApplicationListener：管理配置文件，

2. 1. 比如：`application.properties` 以及 `application.yml`

   2. `application-{profile}.properties`：profile  = dev 、test

   3. 1. 加载优先级
      2. `application-{profile}.properties`
      3. application.properties

Spring Boot 在相对于 ClassPath ： /META-INF/spring.factories

Java SPI : `java.util.ServiceLoader`

~~~java
//Spring SPI：Spring Boot "/META-INF/spring.factories"
# Application Listeners
org.springframework.context.ApplicationListener=\
org.springframework.boot.ClearCachesApplicationListener,\
org.springframework.boot.builder.ParentContextCloserApplicationListener,\
org.springframework.boot.context.FileEncodingApplicationListener,\
org.springframework.boot.context.config.AnsiOutputApplicationListener,\
org.springframework.boot.context.config.ConfigFileApplicationListener,\
org.springframework.boot.context.config.DelegatingApplicationListener,\
org.springframework.boot.context.logging.ClasspathLoggingApplicationListener,\
org.springframework.boot.context.logging.LoggingApplicationListener,\
org.springframework.boot.liquibase.LiquibaseServiceLocatorApplicationListener
//如何控制顺序：实现`Ordered` 以及 标记`@Order`，在 Spring 里面，数值越小，越优先
~~~

### 三 Spring Cloud 事件/监听器

#### 1 BootstrapApplicationListener

1. 加载的优先级 高于 `ConfigFileApplicationListener`，所以 application.properties 文件即使定义也配置不到！原因在于：
   1. `BootstrapApplicationListener ` 第6优先
   2. `ConfigFileApplicationListener` 第11优先
2. step1.负责加载`bootstrap.properties` 或者 `bootstrap.yaml`
3. step2.负责初始化 Bootstrap ApplicationContext，他的 ID = "bootstrap"
   1. java ConfigurableApplicationContext context = builder.run();这行代码很重要
   2. Bootstrap 是一个根 Spring 上下文，parent = null
4. 联想 ClassLoader：ExtClassLoader <- AppClassLoader <- System ClassLoader -> Bootstrap Classloader(null)

~~~java
//Spring Cloud "/META-INF/spring.factories"
# Application Listeners
org.springframework.context.ApplicationListener=\
org.springframework.cloud.bootstrap.BootstrapApplicationListener,\
org.springframework.cloud.bootstrap.LoggingSystemShutdownListener,\
org.springframework.cloud.context.restart.RestartListener
~~~

#### 2 ConfigurableApplicationContext

标准实现类：`AnnotationConfigApplicationContext`

### 四 Bootstrap 配置属性

- Bootstrap 配置文件路径

spring.cloud.bootstrap.location

- 是否允许覆盖远程配置属性

spring.cloud.config.allowOverride

- 自定义 Bootstrap 配置

@BootstrapConfiguration

- 自定义 Bootstrap 配置属性源

PropertySourceLocator

### 五 实现自定义配置

1. 实现`PropertySourceLocator`
2. 暴露该实现作为一个Spring Bean
3. 实现`PropertySource`:

~~~java
@Configuration
@Order(Ordered.HIGHEST_PRECEDENCE)
public static class MyPropertySourceLocator implements PropertySourceLocator {
    @Override
    public PropertySource<?> locate(Environment environment) {
        Map<String,Object> source = new HashMap<>();
        source.put("server.port","9999");
        MapPropertySource propertySource = new MapPropertySource("my-property-source",source);
        return propertySource;
    }
}
~~~

4. 定义并且配置 /META-INF/spring.factories:

 ~~~properties
org.springframework.cloud.bootstrap.BootstrapConfiguration=\
com.gupao.springcloudconfigclient.SpringCloudConfigClientApplication.MyPropertySourceLocator
 ~~~

**注意事项：**

`Environment` 允许出现同名的配置，不过优先级高的胜出

内部实现：`MutablePropertySources` 关联代码：

`private final List<PropertySource<?>> propertySourceList = new CopyOnWriteArrayList<PropertySource<?>>();`

propertySourceList FIFO，它有顺序

可以通过 MutablePropertySources#addFirst 提高到最优先，相当于调用：

`List#add(0,PropertySource);`

### 六 理解 Environment 端点

endpoint : “/env”

#### 1 Env 端点：`EnvironmentEndpoint`

`Environment`关联多个带名称的`PropertySource`

~~~java
//可以参考一下Spring Framework 源码 AbstractRefreshableWebApplicationContext
protected void initPropertySources() {
    ConfigurableEnvironment env = this.getEnvironment();
    if (env instanceof ConfigurableWebEnvironment) {
        ((ConfigurableWebEnvironment)env).initPropertySources(this.servletContext, this.servletConfig);
    }
}
~~~

#### 2 `Environment` 有两种实现方式

1. 普通类型：`StandardEnvironment`
2. Web类型：`StandardServletEnvironment`

`Environment` 

​	 `AbstractEnvironment`

​		`StandardEnvironment`

Enviroment 关联着一个`PropertySources` 实例

`PropertySources` 关联着多个`PropertySource`，并且有优先级。

其中比较常用的`PropertySource` 实现：

1. Java System#getProperties 实现：  名称"systemProperties"，对应的内容 `System.getProperties()`
2. Java System#getenv 实现(环境变量）：  名称"systemEnvironment"，对应的内容 `System.getProperties()`

关于 Spring Boot 优先级顺序，可以参考：https://docs.spring.io/spring-boot/docs/2.0.0.BUILD-SNAPSHOT/reference/htmlsingle/#boot-features-external-config

