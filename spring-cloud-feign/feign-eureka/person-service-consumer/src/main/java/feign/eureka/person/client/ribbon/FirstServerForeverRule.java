package feign.eureka.person.client.ribbon;

import com.netflix.client.config.IClientConfig;
import com.netflix.loadbalancer.AbstractLoadBalancerRule;
import com.netflix.loadbalancer.ILoadBalancer;
import com.netflix.loadbalancer.Server;

import java.util.List;

public class FirstServerForeverRule extends AbstractLoadBalancerRule {

    @Override
    public void initWithNiwsConfig(IClientConfig clientConfig) {
    }

    @Override
    public Server choose(Object key) {

        ILoadBalancer loadBalancer = getLoadBalancer();
        System.err.println("FirstServerForeverRule");
        // 返回三个配置 Server，即：
        // person-service.ribbon.listOfServers = \
        // http://localhost:9090,http://localhost:9090,http://localhost:9090
        List<Server> allServers = loadBalancer.getAllServers();

        return allServers.get(0);
    }

}
