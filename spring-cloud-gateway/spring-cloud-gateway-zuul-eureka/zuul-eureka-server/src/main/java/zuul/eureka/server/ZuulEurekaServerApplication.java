package zuul.eureka.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;

@SpringBootApplication
@EnableEurekaClient
public class ZuulEurekaServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(ZuulEurekaServerApplication.class, args);
    }

}
