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









































