package zuul.eureka.person.api.service;

import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import zuul.eureka.person.api.domain.Person;
import zuul.eureka.person.api.hystrix.PersonServiceFallback;

import java.util.Collection;

/**
 * {@link Person} 服务
 *
 * @author 小马哥 QQ 1191971402
 * @copyright 咕泡学院出品
 * @since 2017/11/5
 */
@FeignClient(value = "zuul-person-service",fallback = PersonServiceFallback.class) // 服务提供方应用的名称
public interface PersonService {

    /**
     * 保存
     *
     * @param person {@link Person}
     * @return 如果成功，<code>true</code>
     */
    @PostMapping(value = "/person/save")
    boolean save(@RequestBody Person person);

    /**
     * 查找所有的服务
     *
     * @return
     */
    @GetMapping(value = "/person/find/all")
    Collection<Person> findAll();

}
