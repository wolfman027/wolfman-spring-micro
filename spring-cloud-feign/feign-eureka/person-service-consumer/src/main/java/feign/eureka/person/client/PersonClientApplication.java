package feign.eureka.person.client;

import feign.eureka.person.api.service.PersonService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.cloud.netflix.feign.EnableFeignClients;
import org.springframework.cloud.netflix.hystrix.EnableHystrix;
import org.springframework.cloud.netflix.ribbon.RibbonClient;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
@EnableEurekaClient
@EnableFeignClients(clients = PersonService.class)
//@RibbonClient(value = "person-service", configuration = PersonClientApplication.class)
//@EnableHystrix
public class PersonClientApplication {
    public static void main(String[] args) {
        SpringApplication.run(PersonClientApplication.class, args);
    }

//    @Bean
//    public FirstServerForeverRule firstServerForeverRule() {
//        return new FirstServerForeverRule();
//    }

}
