## spring cloud feign 服务调用

### 一 申明式 web 服务客户端：feign

申明式：接口声明、Annotation 驱动

Web 服务：HTTP 的方式作为通讯协议

客户端：用于服务调用的存根

Feign：原生并不是 Spring Web MVC的实现，基于JAX-RS（Java REST 规范）实现。Spring Cloud 封装了Feign ，使其支持 Spring Web MVC。`RestTemplate`、`HttpMessageConverter`

\> `RestTemplate `以及 Spring Web MVC 可以显示地自定义 `HttpMessageConverter `实现。

假设，有一个java接口PersonService，Feign可以将其声明是以HTTP方式调用的。



















































