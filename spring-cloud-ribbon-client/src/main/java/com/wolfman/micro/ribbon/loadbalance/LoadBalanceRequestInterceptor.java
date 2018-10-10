package com.wolfman.micro.ribbon.loadbalance;

import org.apache.commons.lang.StringUtils;
import org.omg.PortableInterceptor.Interceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.scheduling.annotation.Scheduled;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.util.*;
import java.util.stream.Collectors;

public class LoadBalanceRequestInterceptor implements ClientHttpRequestInterceptor {

    @Autowired
    private DiscoveryClient discoveryClient;

    private volatile Map<String,Set<String>> targetUrlsCache = new HashMap<>();

    @Scheduled(fixedRate = 10*1000)//10秒钟更新一次
    public void updateTargetUrlsCache(){ //更新目标URL
        Map<String,Set<String>> oldTargetUrlsCache = this.targetUrlsCache;
        //获取当前所有的机器列表
        Map<String,Set<String>> newTargetUrlsCache = new HashMap<>();
        discoveryClient.getServices().forEach(serviceName ->{
            List<ServiceInstance> serviceInstances = discoveryClient.getInstances(serviceName);
            Set<String> newTargetUrls = serviceInstances
                    .stream()
                    .map(s-> s.isSecure()?"https://" + s.getHost() + ":" + s.getPort() :
                            "http://" + s.getHost() + ":" + s.getPort())
                    .collect(Collectors.toSet());
            newTargetUrlsCache.put(serviceName,newTargetUrls);
        });
        //swap
        this.targetUrlsCache = newTargetUrlsCache;
        oldTargetUrlsCache.clear();
    }

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
        //URI /${app-name}/uri
        //URI："/" + serviceName+"/say?message="
        URI reuqestUri = request.getURI();

        String path = reuqestUri.getPath();

        String[] parts = StringUtils.split(path.substring(1),"/");

        String serviceName = parts[0]; //serviceName
        String uri = parts[1];  ///say?message=

        //服务器列表
        //快照
        List<String> targetUrls = new LinkedList<>(targetUrlsCache.get(serviceName));

        //轮询列表
        //选择其中一台服务器
        int size = targetUrls.size();
        //size = 3 ,indext = 0 - 2
        int index = new Random().nextInt(size);
        String targetUrl = targetUrls.get(index);
        //RestTemplate 发送请求到服务器
        //输出响应

        //最终服务器地址 URL
        String actualURL = targetUrl + "/" + uri + "?" + reuqestUri.getQuery();

        System.out.println("本地请求的URL：" + actualURL);


//        List<HttpMessageConverter<?>> messageConverters = Arrays.asList(
//          new ByteArrayHttpMessageConverter(),
//          new StringHttpMessageConverter()
//        );
//
//        RestTemplate restTemplate = new RestTemplate(messageConverters);
//        //响应内容
//        ResponseEntity<InputStream> entity = restTemplate.getForEntity(actualURL,InputStream.class);

        URL url = new URL(actualURL);
        URLConnection urlConnection = url.openConnection();
        //头
        HttpHeaders httpHeaders = new HttpHeaders();
        //响应主体
        InputStream responseBody = urlConnection.getInputStream();

        return new SimpleClientHttpResponse(httpHeaders, responseBody);
    }


    private static class  SimpleClientHttpResponse implements ClientHttpResponse{

        private HttpHeaders httpHeaders;

        private InputStream body;

        private SimpleClientHttpResponse(HttpHeaders httpHeaders, InputStream body) {
            this.httpHeaders = httpHeaders;
            this.body = body;
        }

        @Override
        public HttpStatus getStatusCode() throws IOException {
            return HttpStatus.OK;
        }

        @Override
        public int getRawStatusCode() throws IOException {
            return 200;
        }

        @Override
        public String getStatusText() throws IOException {
            return "200";
        }

        @Override
        public void close() {

        }

        @Override
        public InputStream getBody() throws IOException {
            return body;
        }

        @Override
        public HttpHeaders getHeaders() {
            return httpHeaders;
        }
    }


}
