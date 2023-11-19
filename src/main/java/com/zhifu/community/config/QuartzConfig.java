package com.zhifu.community.config;

import com.zhifu.community.quartz.AlphaJob;
import com.zhifu.community.quartz.PostScoreRefreshJob;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.quartz.JobDetailFactoryBean;
import org.springframework.scheduling.quartz.SimpleTriggerFactoryBean;

//配置在第一次会被读取 ->  写入数据库 -> quartz调用
@Configuration
public class QuartzConfig {

    //BeanFactory：是IOC容器的顶层接口

    //FactoryBean：可简化Bean的实例化过程
    //1.通过FactoryBean封装Bean的实例化过程；
    //2.将FactoryBean装配到Spring容器中；（最重要的一步，也是需要代码实现的一步）
    //3.将FactoryBean注入给其他的Bean;
    //4.则该Bean(也就是3.中的其他的Bean)得到的是FactoryBean所管理的对象实例

    //配置JobDetail
    //@Bean
    public JobDetailFactoryBean alphaJobDetail(){
        JobDetailFactoryBean factoryBean = new JobDetailFactoryBean();
        factoryBean.setJobClass(AlphaJob.class);
        factoryBean.setName("alphaJob");
        factoryBean.setGroup("alphaJobGroup");
        factoryBean.setDurability(true);//设置该任务是否长久保存
        factoryBean.setRequestsRecovery(true);//设置该任务是否可恢复
        return factoryBean;
    }

    // 配置Trigger( SimpleTriggerFactoryBean, CronTriggerFactoryBean,比较复杂，通过特殊表达式进行设置，实现高级定制 )
    //@Bean
    public SimpleTriggerFactoryBean alphaTrigger(JobDetail alphaJobDetail){
        SimpleTriggerFactoryBean factoryBean = new SimpleTriggerFactoryBean();
        factoryBean.setJobDetail(alphaJobDetail);
        factoryBean.setName("alphaTrigger");
        factoryBean.setGroup("alphaTriggerGroup");
        factoryBean.setRepeatInterval(3000);//设置重复时间间隔
        factoryBean.setJobDataMap(new JobDataMap());//Trigger底层需要存储Job的一些状态，指定存储的数据对象
        return factoryBean;
    }

    //刷新帖子分数任务
    @Bean
    public JobDetailFactoryBean postScoreRefreshJobDetail(){
        JobDetailFactoryBean factoryBean = new JobDetailFactoryBean();
        factoryBean.setJobClass(PostScoreRefreshJob.class);
        factoryBean.setName("postScoreRefreshJob");
        factoryBean.setGroup("communityJobGroup");
        factoryBean.setDurability(true);//设置该任务是否长久保存
        factoryBean.setRequestsRecovery(true);//设置该任务是否可恢复
        return factoryBean;
    }
    @Bean
    public SimpleTriggerFactoryBean postScoreRefreshTrigger(JobDetail postScoreRefreshJobDetail){
        SimpleTriggerFactoryBean factoryBean = new SimpleTriggerFactoryBean();
        factoryBean.setJobDetail(postScoreRefreshJobDetail);
        factoryBean.setName("postScoreRefreshTrigger");
        factoryBean.setGroup("communityTriggerGroup");
        factoryBean.setRepeatInterval(1000 * 60 * 5);//设置重复时间间隔,5min
        factoryBean.setJobDataMap(new JobDataMap());//Trigger底层需要存储Job的一些状态，指定存储的数据对象
        return factoryBean;
    }

}
