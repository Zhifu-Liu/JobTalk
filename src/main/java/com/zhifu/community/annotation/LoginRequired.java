package com.zhifu.community.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

//表明该注解用在方法之上
@Target(ElementType.METHOD)
//retention 保留，保持
//表明该注解有效或者运行在什么时期，如下为运行时期
@Retention(RetentionPolicy.RUNTIME)
public @interface LoginRequired {

    //注解内部什么都不用写
}
