package zuul.eureka.person.service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.cloud.netflix.hystrix.EnableHystrix;

/**
 * {@PersonService} 提供者应用
 *
 * @author 小马哥 QQ 1191971402
 * @copyright 咕泡学院出品
 * @since 2017/11/5
 */
@SpringBootApplication
@EnableEurekaClient
@EnableHystrix
public class ZuulEurekaPersonServiceProviderApplication {

    public static void main(String[] args) {
        SpringApplication.run(ZuulEurekaPersonServiceProviderApplication.class,args);
    }

}
