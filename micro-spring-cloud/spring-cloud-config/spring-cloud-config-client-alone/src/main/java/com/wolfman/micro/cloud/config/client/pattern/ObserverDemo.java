package com.wolfman.micro.cloud.config.client.pattern;

import java.util.*;

/**
 * 发布/订阅模式
 */
public class ObserverDemo {

    public static void main(String[] args) {
        MyObserver observer = new MyObserver();
        observer.addObserver(new Observer() {
            @Override
            public void update(Observable o, Object value) {
                System.out.println(value);
            }
        });
        observer.setChanged();
        observer.notifyObservers("hello");
    }

    private static void echoIterator(){
        List<Integer> values = Arrays.asList(1,2,3,4,5);
        Iterator<Integer> integerIterator =  values.iterator();
        //这种就是拉模式，是自己去获取的
        //通过循环，主动的去获取
        while (integerIterator.hasNext()){
            System.out.println(integerIterator.next());
        }
    }

    public static class MyObserver extends Observable{
        protected synchronized void setChanged() {
            super.setChanged();
        }
    }

}
