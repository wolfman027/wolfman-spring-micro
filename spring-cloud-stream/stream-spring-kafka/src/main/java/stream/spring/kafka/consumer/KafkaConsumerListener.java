package stream.spring.kafka.consumer;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.PartitionOffset;
import org.springframework.kafka.annotation.TopicPartition;
import org.springframework.stereotype.Component;

@Component
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
