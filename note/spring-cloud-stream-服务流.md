## spring cloud stream 服务流

Spring Cloud Stream：流，整合了大多数消息中间件。

### 一 spring boot kafka

官方网站：http://kafka.apache.org/

下载地址：http://kafka.apache.org/downloads

#### 1 主要用途

- 消息中间件
- 流式计算处理
- 日志

#### 2 同类产品比较

1. ActiveMQ：JMS（Java Message Service） 规范实现
2. RabbitMQ：AMQP（Advanced Message Queue Protocol）规范实现
3. Kafka：并非某种规范实现，它灵活和性能相对是优势

#### 3 具体应用

官方文档：https://docs.spring.io/spring-kafka/reference/htmlsingle/

**设计模式：**

Spring 社区对 data(`spring-data`) 操作，有一个基本的模式， Template 模式：

1. JDBC : `JdbcTemplate`
2. Redis : `RedisTemplate`
3. Kafka : `KafkaTemplate`
4. JMS : `JmsTemplate`
5. Rest: `RestTemplate`
6. XXXTemplate 一定实现 XXXOpeations
7. KafkaTemplate 实现了 KafkaOperations

- 添加 maven 依赖

~~~xml
<dependency>
  <groupId>org.springframework.kafka</groupId>
  <artifactId>spring-kafka</artifactId>
</dependency>
~~~

- 自动装配器：`KafkaAutoConfiguration` 自动装配如下代码

~~~java
@Bean
@ConditionalOnMissingBean(KafkaTemplate.class)
public KafkaTemplate<?, ?> kafkaTemplate(
      ProducerFactory<Object, Object> kafkaProducerFactory,
      ProducerListener<Object, Object> kafkaProducerListener) {
   KafkaTemplate<Object, Object> kafkaTemplate = 
                  new KafkaTemplate<Object, Object>(kafkaProducerFactory);
   kafkaTemplate.setProducerListener(kafkaProducerListener);
   kafkaTemplate.setDefaultTopic(this.properties.getTemplate().getDefaultTopic());
   return kafkaTemplate;
}
~~~

- 创建生产者

~~~properties
# 增加生产者配置

## 设置 kafka topic
kafka.topic = mhxy

## Spring Kafka 配置信息
spring.kafka.bootstrapServers = 39.107.32.43:9092
### Kafka 生产者配置
# spring.kafka.producer.bootstrapServers = localhost:9092
spring.kafka.producer.keySerializer =org.apache.kafka.common.serialization.StringSerializer
spring.kafka.producer.valueSerializer =org.apache.kafka.common.serialization.StringSerializer
~~~

~~~java
// 编写发送端实现
@RestController
public class KafkaProducerController {

    private final KafkaTemplate<String, String> kafkaTemplate;

    private final String topic;

    @Autowired
    public KafkaProducerController(KafkaTemplate<String, String> kafkaTemplate,
                                   @Value("${kafka.topic}") String topic) {
        this.kafkaTemplate = kafkaTemplate;
        this.topic = topic;
    }

    @PostMapping("/message/send")
    public Boolean sendMessage(@RequestParam String message) {
        kafkaTemplate.send(topic, message);
        return true;
    }

}
~~~

- 创建消费者

~~~properties
# 增加消费者配置
### Kafka 消费者配置
spring.kafka.consumer.groupId = gupao-1
spring.kafka.consumer.keyDeserializer =org.apache.kafka.common.serialization.StringDeserializer
spring.kafka.consumer.valueDeserializer =org.apache.kafka.common.serialization.StringDeserializer
~~~

~~~java
// 编写消费端实现
public class KafkaConsumerListener {
    @KafkaListener(topicPartitions = @TopicPartition(
            topic = "${kafka.topic}",
            partitionOffsets = @PartitionOffset(
                    partition = "0" ,
                    initialOffset = "0")))
    public void onMessage(String message) {
        System.out.println("Kafka 消费者监听器，接受到消息：" + message);
    }
}
~~~

### 二 Spring Cloud Stream

![](https://github.com/wolfJava/wolfman-spring-micro/blob/master/spring-cloud-stream/img/stream-1.jpg?raw=true)

官方网站：https://cloud.spring.io/spring-cloud-stream/

RabbitMQ：AMQP、JMS 规范

Kafka：相对松散的消息队列协议

- 基本概念

1. Source：来源，近义词：Producer、Publisher
2. Sink：接收器，近义词：Consumer、Subscriber
3. Processor：对于上流而言是 Sink，对于下流而言是 Source

- Reactive Streams

1. Publisher
2. Subscriber
3. Processor

#### 1 Spring Cloud Stream Kafka Binder

Spring Cloud Stream Binder : http://kafka.apache.org/

消息大致分为两个部分：

1. 消息头（Headers）
2. 消息体（Body/Payload）

##### 1.1 定义标准消息发送源

~~~java
@Component
@EnableBinding({Source.class})
public class MessageProducerBean {

    @Autowired
    @Qualifier(Source.OUTPUT) // Bean 名称
    private MessageChannel messageChannel;

    @Autowired
    private Source source;
}
~~~

##### 1.2 自定义标准消息发送源

~~~java
public interface MessageSource {
    /**
     * 消息来源的管道名称："gupao"
     */
    String OUTPUT = "gupao";

    @Output(OUTPUT)
    MessageChannel gupao();

}
~~~

~~~java
@Component
@EnableBinding({Source.class,MessageSource.class})
public class MessageProducerBean {

    @Autowired
    @Qualifier(Source.OUTPUT) // Bean 名称
    private MessageChannel messageChannel;

    @Autowired
    private Source source;

    @Autowired
    private MessageSource messageSource;

    @Autowired
    @Qualifier(MessageSource.OUTPUT) // Bean 名称
    private MessageChannel gupaoMessageChannel;

    /**
     * 发送消息
     * @param message 消息内容
     */
    public void send(String message){
        // 通过消息管道发送消息
//        messageChannel.send(MessageBuilder.withPayload(message).build());
        source.output().send(MessageBuilder.withPayload(message).build());
    }

    /**
     * 发送消息到 Gupao
     * @param message 消息内容
     */
    public void sendToGupao(String message){
        // 通过消息管道发送消息
        gupaoMessageChannel.send(MessageBuilder.withPayload(message).build());
    }

}
~~~

##### 1.3 实现标准 `Sink` 监听

~~~java
@Component
@EnableBinding({Sink.class})
public class MessageConsumerBean {

    @Autowired
    @Qualifier(Sink.INPUT) // Bean 名称
    private SubscribableChannel subscribableChannel;

    @Autowired
    private Sink sink;
    
}
~~~

##### 1.4 通过 `SubscribableChannel` 订阅消息

~~~java
// 当字段注入完成后的回调
    @PostConstruct
    public void init(){
        // 实现异步回调
        subscribableChannel.subscribe(new MessageHandler() {
            @Override
            public void handleMessage(Message<?> message) throws MessagingException {

                System.out.println(message.getPayload());

            }
        });
    }
~~~

##### 1.5 通过 `@ServiceActivator` 订阅消息

~~~java
//通过@ServiceActivator
    @ServiceActivator(inputChannel = Sink.INPUT)
    public void onMessage(Object message) {
        System.out.println("@ServiceActivator : " + message);
    }
~~~

##### 1.5 通过 `@StreamListener` 订阅消息

~~~java
@StreamListener(Sink.INPUT)
    public void onMessage(String message){
        System.out.println("@StreamListener : " + message);
    }
~~~



### 三 相关技术

#### Reactive Streams

publiser

Subscriber

Processor

元编程：基于编程的编程：Reflection，Function、Lambda

```
Stream
	.of(1,2,3,4,5,6) //生产
	.map(String::valueOf) //处理
	.forEach(System.out::println); //消费
```

//for-each 必须实现 java.lang.Iterable，例如：[]、Collection

#### Spring Cloud Data Flow

### 四 主要内容

#### Spring Cloud Stream 整合 RabbitMQ

**代码详见 spring-cloud-client-application 与 spring-cloud-server-applicaiton**

##### 增加依赖

```
<!-- 增加 stream rabbit 依赖 -->
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-stream-binder-rabbit</artifactId>
</dependency>

<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-stream-rabbit</artifactId>
</dependency>
```

##### 分析依赖

- **间接依赖：Spring Integration，相关文档：**<https://docs.spring.io/spring-integration/docs/5.0.7.RELEASE/reference/html/amqp.html>
- Spring Message
- Spring AMQP
  - RabbitMQ
  - RabbitTemplate 自动装配

##### 配置项

- RabbitBindingProperties

> 小技巧：
>
> Spring中，大多数都是 *Template 实现 * Operations
>
> 例如：jdbcTemplate、RedisTemplate、RabbitTemplate、RestTemplate、KafkaTemplate

##### 务必在消息处理中做幂等性处理

```
	@Autowired
    private SimpleMessageReceiver simpleMessageReceiver;

    @PostConstruct
    public void init(){ //接口编程
        SubscribableChannel subscribableChannel = simpleMessageReceiver.gupao();
        subscribableChannel.subscribe(message -> {
            MessageHeaders headers = message.getHeaders();
            String encoding = (String) headers.get("charset-encoding");
            String text = (String) headers.get("content-type");
            byte[] content = (byte[]) message.getPayload();
            try {
                System.out.println("接收到消息：" + new String(content,encoding));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        });
    }

    @StreamListener("gupao2018")
    public void onMessage(byte[] data){//注解编程
        System.out.println("onMessage(byte[]):"+data);

    }

    @StreamListener("gupao2018")
    public void onMessage(String data){//注解编程

        System.out.println("onMessage(String):"+data);

    }

    @ServiceActivator(inputChannel = "gupao2018")
    public void onServiceActivator(String data){//Spring Integration 注解驱动

        System.out.println("onServiceActivator(String):"+data);

    }

    /**
     * 同一种编程模型，都会收到
     * 不同的编程模型，循环收到
     *
     * @StreamListener 优先于 @ServiceActivator 优先于 注解编程
     *
     */
```

**注意：**相同的编程模型重复执行，例如：@StreamListener。不同的编程模型轮流执行