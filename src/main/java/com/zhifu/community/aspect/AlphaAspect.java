package com.zhifu.community.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.stereotype.Component;

//@Component//交给Spring容器进行管理
//@Aspect//表明这是一个切面组件，不是普通的组件
public class AlphaAspect {
    //定义切点 pointCut
    //切点是通过方法上的该注解来进行定义
    @Pointcut("execution(* com.zhifu.community.service.*.*(..))")
    //第一个* 表示返回值不限， 第二个* 表示service下的任何类， 第三个* 表示类下的方法， (..)表示方法的输入参数类型不限
    public void pointcut(){
    }

    //定义通知，通知有五类

    //在连接点的一开始 记日志
    @Before("pointcut()")
    public void before(){
        System.out.println("before");
    }

    //在连接点的后面 记日志
    @After("pointcut()")
    public void after(){
        System.out.println("after");
    }

    //在有了返回值以后 进行逻辑处理，例如记日志
    @AfterReturning("pointcut()")
    public void afterReturning(){
        System.out.println("afterReturning");
    }

    //在抛出异常后 进行逻辑处理，例如记日志
    @AfterThrowing("pointcut()")
    public void afterThrowing(){
        System.out.println("afterThrowing");
    }

    //在连接点的前后都 进行逻辑处理，例如记日志
    @Around("pointcut()")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable{
        //在连接点之前，进行逻辑处理
        System.out.println("around before");

        //调目标对象被处理的方法逻辑，就是调我们要处理的目标组件的方法，即连接点
        Object obj = joinPoint.proceed();

        //在连接点之后，进行逻辑处理
        System.out.println("around after");

        return obj;
    }

}
