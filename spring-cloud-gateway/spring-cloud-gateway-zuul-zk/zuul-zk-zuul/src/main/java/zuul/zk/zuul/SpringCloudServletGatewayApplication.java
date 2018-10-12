package zuul.zk.zuul;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;
import zuul.zk.zuul.loadbalancer.ZookeeperLoadBalancer;

@SpringBootApplication
@EnableDiscoveryClient
@ServletComponentScan(basePackages = "zuul.zk.zuul.servlet")
@EnableScheduling
public class SpringCloudServletGatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringCloudServletGatewayApplication.class,args);
    }

    @Bean
    public ZookeeperLoadBalancer zookeeperLoadBalancer(DiscoveryClient discoveryClient) {
        return new ZookeeperLoadBalancer(discoveryClient);
    }


}
