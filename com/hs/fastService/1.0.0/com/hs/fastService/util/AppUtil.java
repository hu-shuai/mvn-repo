package com.hs.fastService.util;

import org.springframework.context.ApplicationContext;

public class AppUtil {

    public static ApplicationContext context;

    //通过name获取 Bean.
    public static Object getBean(String name){
        return context.getBean(name);
    }

    //通过class获取Bean.
    public static <T> T getBean(Class<T> clazz){
        return context.getBean(clazz);
    }

    //通过name,以及Clazz返回指定的Bean
    public static <T> T getBean(String name,Class<T> clazz){
        return context.getBean(name, clazz);
    }
}
