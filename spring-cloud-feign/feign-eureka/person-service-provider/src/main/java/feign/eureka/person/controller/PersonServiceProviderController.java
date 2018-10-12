package feign.eureka.person.controller;

import feign.eureka.person.api.domain.Person;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@RestController
public class PersonServiceProviderController {

    private final Map<Long, Person> persons = new ConcurrentHashMap<>();

    /**
     * 保存
     *
     * @param person {@link Person}
     * @return 如果成功，<code>true</code>
     */
    @PostMapping(value = "/person/save")
    public boolean savePerson(@RequestBody Person person) {
        return persons.put(person.getId(), person) == null;
    }

    /**
     * 查找所有的服务
     */
    @GetMapping(value = "/person/find/all")
    public Collection<Person> findAllPersons() {
        System.out.println("/person/find/all");
        return persons.values();
    }


}
