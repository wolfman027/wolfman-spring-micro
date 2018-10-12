package stream.cloud.kafka.binder.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import stream.cloud.kafka.binder.stream.producer.MessageProducer;

/**
 * Kafka 生产者 Controller
 *
 * @author 小马哥 QQ 1191971402
 * @copyright 咕泡学院出品
 * @since 2017/11/12
 */
@RestController
public class KafkaProducerController {


    private final MessageProducer messageProducer;

    private final String topic;

    @Autowired
    public KafkaProducerController(MessageProducer messageProducer,
                                   @Value("${kafka.topic}") String topic) {
        this.messageProducer = messageProducer;
        this.topic = topic;
    }



    /**
     * 通过{@link MessageProducer} 发送
     * @param message
     * @return
     */
    @GetMapping("/message/send")
    public Boolean send(@RequestParam String message) {
        messageProducer.send(message);
        return true;
    }

}
