package feign.zk.annotation;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.SingletonBeanRegistry;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.env.Environment;
import org.springframework.core.type.AnnotationMetadata;

import java.lang.reflect.Proxy;
import java.util.Map;
import java.util.stream.Stream;

import static org.springframework.core.annotation.AnnotationUtils.findAnnotation;

public class FeignRestClientsRegistrar implements ImportBeanDefinitionRegistrar,
        BeanFactoryAware, EnvironmentAware {

    private BeanFactory beanFactory;

    private Environment environment;

    @Override
    public void registerBeanDefinitions(AnnotationMetadata metadata, BeanDefinitionRegistry registry) {

        ClassLoader classLoader = metadata.getClass().getClassLoader();

        Map<String, Object> attributes = metadata.getAnnotationAttributes(FeignEnableRestClient.class.getName());

        //attributes -> {clients：FeignSayingRestService }
        Class<?>[] clientClasses = (Class<?>[]) attributes.get("clients");

        //接口类对象数据
        //筛选所有接口
        Stream.of(clientClasses)
                .filter(Class::isInterface) //仅选择接口
                .filter(interfaceClass ->{
                    return findAnnotation(interfaceClass,FeignRestClient.class) != null; //仅选择标注 @FeignRestClient
                })
                .forEach(feignRestClientClass ->{
                    //获取 @FeignRestClient 元信息
                    FeignRestClient feignRestClient = findAnnotation(feignRestClientClass,FeignRestClient.class);
                    //获取 应用名称(处理占位符)
                    String serviceName = environment.resolvePlaceholders(feignRestClient.name());
                    //RestTemplate -> serviceName/rui/param=...

                    // @RestClient 接口编程 JDK 动态代理
                    Object proxy = Proxy.newProxyInstance(classLoader, new Class[]{feignRestClientClass},
                            new FeignRequestMappingMethodInvocationHandler(serviceName,beanFactory));

                    String beanName = "FeignRestClient." + serviceName;

                    //注入poxy 实现方法2
                    if (registry instanceof SingletonBeanRegistry){
                        SingletonBeanRegistry singletonBeanRegistry = (SingletonBeanRegistry) registry;
                        singletonBeanRegistry.registerSingleton(beanName,proxy);
                    }
                    //利用factoryBean来进行注册的
//                    registerBeanByFactoryBean(serviceName,proxy,feignRestClientClass,registry);
                });
    }

    private static void registerBeanByFactoryBean(String serviceName,
                                                  Object proxy, Class<?> feignRestClientClass, BeanDefinitionRegistry registry){

        // 将 @RestClient 接口代理实现注册为 Bean(@Autowired)
        // BeanDefinitionRegistry registry
        String beanName = "FeignRestClient." + serviceName;
        BeanDefinitionBuilder beanDefinitionBuilder =
                BeanDefinitionBuilder.genericBeanDefinition(FeignRestClientClassFactoryBean.class);
        /**
         * <bean class="User">
         *     <constructor-arg>${}</constructor-arg>
         * </bean>
         */
        //增加第一个构造器参数引用：proxy
        beanDefinitionBuilder.addConstructorArgValue(proxy);
        //增加第二个构造器参数引用：feignRestClientClass
        beanDefinitionBuilder.addConstructorArgValue(feignRestClientClass);
        BeanDefinition beanDefinition = beanDefinitionBuilder.getBeanDefinition();
        registry.registerBeanDefinition(beanName,beanDefinition);
    }



    private static class FeignRestClientClassFactoryBean implements FactoryBean {

        private final Object proxy;

        private final Class<?> feignRestClientClass;

        private FeignRestClientClassFactoryBean(Object proxy, Class<?> feignRestClientClass) {
            this.proxy = proxy;
            this.feignRestClientClass = feignRestClientClass;
        }

        @Override
        public Object getObject() throws Exception {
            return proxy;
        }

        @Override
        public Class<?> getObjectType() {
            return feignRestClientClass;
        }
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        this.beanFactory = beanFactory;
    }

    @Override
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }

}
