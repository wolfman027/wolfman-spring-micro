package com.wolfman.micro.cloud.config.client.pattern;

import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class SpringEventListenerDemo {

    public static void main(String[] args) {
        //Annotation 驱动的Spring上下文
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
        //step1.注册监听器,只关注MyApplicationEvent这个事件
        context.addApplicationListener(new ApplicationListener<MyApplicationEvent>() {
            //step3.监听器得到事件
            @Override
            public void onApplicationEvent(MyApplicationEvent myApplicationEvent) {
                System.out.println("---------------------------");
                System.out.println("接收到事件：" + myApplicationEvent.getSource() + " @@@@@ " +  myApplicationEvent.applicationContext);
                System.out.println("---------------------------");
            }
        });
        context.refresh();
        //step2.发布事件
        context.publishEvent(new MyApplicationEvent(context,"Hello,World"));
        context.publishEvent(new MyApplicationEvent(context,"1"));
        context.publishEvent(new MyApplicationEvent(context,"2"));
        context.publishEvent(new MyApplicationEvent(context,"3"));
    }

    private static class MyApplicationEvent extends ApplicationEvent{

        private final ApplicationContext applicationContext;

        public ApplicationContext getApplicationContext() {
            return applicationContext;
        }

        public MyApplicationEvent(ApplicationContext applicationContext, Object source) {
            super(source);
            this.applicationContext = applicationContext;
        }

    }

}
