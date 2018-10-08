## spring cloud config 配置管理

### 一 理解图

![](https://github.com/wolfJava/wolfman-spring-micro/blob/master/micro-spring-cloud/spring-cloud-config/img/spring-cloud-config-1.jpg?raw=true)

![](https://github.com/wolfJava/wolfman-spring-micro/blob/master/micro-spring-cloud/spring-cloud-config/img/spring-cloud-config-2.jpg?raw=true)

#### 1 国内知名开源项目

百度：Disconf、携程：Apollo、阿里：Nacos

#### 2 国内外知名开源项目

Spring Cloud Config、Netfix Archaius、Apache Zookeeper

### 二 客户端

#### 1 配置三方库 - commons-configuration三方包

- Configuration：提供大多数常见类型的 Value 转换
  - PropertiesConfiguration：将 Properties 作为 Configration 配置
  - MapConfiguration
    - EnvironmentConfiguration：OS 环境变量
    - SystemConfiguration：Java 系统属性
  - CompositeConfiguration（组合配置）

核心概念：配置源、他们优先级次序、配置转换能力

HTTP资源算不算一个配置？

配置源：文件、HTTP 资源、数据源、Git ->URL-> file:///，http://，jdbc://，git://

#### 2 Spring Environment

```sequence
Environment -> ConfigurableEnvironment: 父子层次
ConfigurableEnvironment -> MutablePropertySources: 获取可变多个配置源
MutablePropertySources -> List PropertySource: 包含多个 PropertySource
```

PropertySource：配置源

- MapPropertySource
  - PropertiesPropertySource
- CompositePropertySource：组合
- SystemEnvironmentPropertySource：环境变量

### 三 服务端

#### 1 基于 git 实现

版本化配置

/应用名称/profile/${label}	

/应用名称/profile/ = /应用名称/profile/master

/应用名/ = /应用名.properties

${label}：分支



 Spring Cloud Config 实现了一套完整的配置管理API设计

Git实现的缺陷：

- 复杂的版本更新机制（Git 仓库）
  - 版本
  - 分支
  - 提交
  - 配置
- 憋足的内容更新（实时性不高）
  - 客户端第一次启动拉取
  - 需要整合 BUS 做更新通知

#### 2 设计原理

分析@EnableConfigServer

```java
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import(ConfigServerConfiguration.class)
public @interface EnableConfigServer {
}
```

实际配置类：ConfigServerConfiguration

```java
@Configuration
public class ConfigServerConfiguration {
	class Marker {}

	@Bean
	public Marker enableConfigServerMarker() {
		return new Marker();
	}
}
```

```java
@Configuration
@ConditionalOnBean(ConfigServerConfiguration.Marker.class)
@EnableConfigurationProperties(ConfigServerProperties.class)
@Import({ EnvironmentRepositoryConfiguration.class, CompositeConfiguration.class, ResourceRepositoryConfiguration.class,
		ConfigServerEncryptionConfiguration.class, ConfigServerMvcConfiguration.class })
public class ConfigServerAutoConfiguration {

}

```

当应用配置类标注了

- @EnableConfigServer 
  - 导入ConfigServerConfiguration
    - 注册 Marker Bean
      - 作为 ConfigServerAutoConfiguration 条件之一

#### 3 案例分析 JDBC 实现

- jdbcTemplate Bean 来源

  - JdbcTemplateAutoConfiguration

- SQL 来源

  - JdbcEnvironmentProperties
    - spring.cloud.config.server.jdbc
      - 不配置：默认：DEFAULT_SQL：SELECT KEY, VALUE from PROPERTIES where APPLICATION=? and PROFILE=? and LABEL=?


| KEY  | VALUE      | APPLICATION | PROFILE | LABEL  |
| ---- | ---------- | ----------- | ------- | ------ |
| name | mercyblitz | config      | default | master |
| name | xiaomage   | config      | test    | master |

本质说明：

​	JDBC连接技术

​	DB 存储介质

​	EnvironmentRepository 核心接口



思考：是否可以自定义EnvironmentRepository 实现？

前提：如何激活自定义的EnvironmentRepository 实现

找到了为什么默认是 Git 作为配置仓库的原因：

```java
@Configuration
@ConditionalOnMissingBean(value = EnvironmentRepository.class, search = SearchStrategy.CURRENT)
class DefaultRepositoryConfiguration {
	...
	@Bean
	public MultipleJGitEnvironmentRepository defaultEnvironmentRepository(
	        MultipleJGitEnvironmentRepositoryFactory gitEnvironmentRepositoryFactory,
			MultipleJGitEnvironmentProperties environmentProperties) throws Exception {
		return gitEnvironmentRepositoryFactory.build(environmentProperties);
	}
}
```

当 Spring 应用上下文没有出现 EnvironmentRepository Bean 的时候，那么，默认不会激活DefaultRepositoryConfiguration （Git 实现），否则采用自定义实现。

#### 4 自定义实现

自定义 EnvironmentRepository Bean

```java
@Bean
    public EnvironmentRepository environmentRepository(){
//        return new EnvironmentRepository() {
//            @Override
//            public Environment findOne(String application, String profile, String label) {
//                Environment environment = new Environment();
//                return null;
//            }
//        }

        return (String application, String profile, String label) ->{
            Environment environment = new Environment("default",profile);
            List<PropertySource> propertySources = environment.getPropertySources();

            Map<String,Object> source = new HashMap<>();

            source.put("name","胡昊");

            PropertySource propertySource = new PropertySource("map",source);
            //追加 propertySource
            propertySources.add(propertySource);

            return environment;
        };
    }
```

以上实现将失效：DefaultRepositoryConfiguration 装配。

#### 5 HTTP 请求模式

/application/profile/${label}

@Controller 或者 @RestController

@RequestMapping("/{application}/{profile}/{label}") 在EnvironmentController类中就能找到它配置好的地址。

/config/test/master

config：application

test：profile

master：label

### 四 比较Spring Cloud 内建配置仓储的实现

Git 早放弃

JDBC 太简单

Zookeeper 比较适合做分布式配置

自定义是高端玩家









