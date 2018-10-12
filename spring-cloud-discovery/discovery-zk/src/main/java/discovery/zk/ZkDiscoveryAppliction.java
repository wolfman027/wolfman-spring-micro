package discovery.zk;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient  //尽可能使用 @EnableDiscoveryClient
public class ZkDiscoveryAppliction {

    public static void main(String[] args) {
        SpringApplication.run(ZkDiscoveryAppliction.class, args);
    }
}