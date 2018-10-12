package feign.zk.annotation;

import java.lang.annotation.*;

/**
 * feign rest client 注解
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface FeignRestClient {

    /**
     * REST 服务应用名称
     * @return
     */
    String name();

}
