package feign.eureka.person.api.hystrix;

import feign.eureka.person.api.domain.Person;
import feign.eureka.person.api.service.PersonService;

import java.util.Collection;
import java.util.Collections;

/**
 * {@link PersonService} Fallback 实现
 */
public class PersonServiceFallback implements PersonService {

    @Override
    public boolean save(Person person) {
        return false;
    }

    @Override
    public Collection<Person> findAll() {
        return Collections.emptyList();
    }

}
