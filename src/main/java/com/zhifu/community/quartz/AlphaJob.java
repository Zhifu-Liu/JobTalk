package com.zhifu.community.quartz;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;


//关于quartz的核心组件的说明
//Job：定义待执行任务的具体内容
//JobDetail：对Job的配置
//Trigger：包含JobDetail的配置，并对任务的定时执行进行配置

// Quartz默认将Job的配置信息放在内存中进行读取
// 可以通过在SpringBoot的properties文件中进行配置，来使得将Job的配置信息放在数据库中，以便后续进行读取
//配置信息QuartzConfig，在Quartz第一次执行时便被读取到数据库中的表里，之后的执行不会再次读取

public class AlphaJob implements Job {
    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        System.out.println(Thread.currentThread().getName() + ":execute a quartz job!");
    }
}
