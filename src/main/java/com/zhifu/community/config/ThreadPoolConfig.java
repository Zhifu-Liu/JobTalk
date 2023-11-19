package com.zhifu.community.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

//该配置类是 Spring的ThreadPoolTaskScheduler 的配置类
@Configuration

@EnableScheduling//使能执行定时任务
@EnableAsync//让该方法在多线程环境下，被异步的调用
public class ThreadPoolConfig {
}
