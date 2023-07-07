package com.zhifu.community.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;

@Configuration
public class RedisConfig {

    //当定义一个Bean时，在该Bean的方法参数内输入redis连接工厂类（该类是bean，已经被Spring容器装配），Spring容器会自动识别并注入
    @Bean
    public RedisTemplate<String ,Object> redisTemplate(RedisConnectionFactory factory){
        RedisTemplate<String ,Object> template = new RedisTemplate<>();
        template.setConnectionFactory(factory);//设置template的链接工厂

        //设置key的序列化方法
        template.setKeySerializer(RedisSerializer.string());
        //设置value的序列化方式
        template.setValueSerializer(RedisSerializer.json());
        //有一类value比较特殊，就是哈希，其属于哈希中的哈希，因此，需要单独对hash的value进行序列化方式的设置
        //设置hash的key的序列化方式
        template.setHashKeySerializer(RedisSerializer.string());
        //设置hash的value的序列化方式
        template.setHashValueSerializer(RedisSerializer.json());

        //使得上述设置生效
        template.afterPropertiesSet();
        return template;

    }
}
