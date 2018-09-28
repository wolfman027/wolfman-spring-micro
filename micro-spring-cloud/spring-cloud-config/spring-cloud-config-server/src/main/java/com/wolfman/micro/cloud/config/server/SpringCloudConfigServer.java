package com.wolfman.micro.cloud.config.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.config.environment.Environment;
import org.springframework.cloud.config.environment.PropertySource;
import org.springframework.cloud.config.server.EnableConfigServer;
import org.springframework.cloud.config.server.environment.EnvironmentRepository;
import org.springframework.context.annotation.Bean;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SpringBootApplication
@EnableConfigServer
public class SpringCloudConfigServer {

    public static void main(String[] args) {

        SpringApplication.run(SpringCloudConfigServer.class,args);

    }

//    @Bean
//    public EnvironmentRepository environmentRepository(){
////        return new EnvironmentRepository() {
////            @Override
////            public Environment findOne(String application, String profile, String label) {
////                Environment environment = new Environment();
////                return null;
////            }
////        }
//
//        return (String application, String profile, String label) ->{
//            Environment environment = new Environment("default",profile);
//            List<PropertySource> propertySources = environment.getPropertySources();
//
//            Map<String,Object> source = new HashMap<>();
//
//            source.put("name","胡昊-zdy");
//            source.put("age","26-zdy");
//            source.put("sex","男-zdy");
//
//            PropertySource propertySource = new PropertySource("map",source);
//            //追加 propertySource
//            propertySources.add(propertySource);
//
//            return environment;
//        };
//    }











}
