package com.zhifu.community;

import com.alibaba.fastjson.JSONObject;
import com.zhifu.community.entity.Event;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import javax.json.JsonObject;
import java.util.HashMap;

@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)
public class KafkaTests {
    @Autowired
    private KafkaProducer kafkaProducer;

    @Test
    public void testKafka(){

        HashMap<String,Object> map = new HashMap<>();
        map.put("name","GG-Bond");
        map.put("age",18);
        HashMap<String,Object> innerMap = new HashMap<>();

        HashMap<String,Object> innerInnerMap = new HashMap<>();
        innerInnerMap.put("pingpang", 0.035);
        innerInnerMap.put("basketball", "ok");

        innerMap.put("sports",innerInnerMap);
        innerMap.put("swim",1.00001);
        innerMap.put("sing","average");

        map.put("skill",innerMap);

        kafkaProducer.sendMessage("jiashi", JSONObject.toJSONString(map));
        //kafkaProducer.sendMessage("test","在吗？");
        try {
            Thread.sleep(1000*5);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testKafka2(){

        HashMap<String,Object> map = new HashMap<>();
        map.put("name","Jack");
        map.put("age",17);
        HashMap<String,Object> innerMap = new HashMap<>();

//        HashMap<String,Object> innerInnerMap = new HashMap<>();
//        innerInnerMap.put("pingpang", 0);
//        innerInnerMap.put("basketball", "excellent");

        //innerMap.put("sports",innerInnerMap);
        innerMap.put("boxing","best");
        innerMap.put("swim","bad");
        innerMap.put("sing","good");

        map.put("skill",innerMap);

        kafkaProducer.sendMessage("jiashi2", JSONObject.toJSONString(map));
        //kafkaProducer.sendMessage("test","在吗？");
        try {
            Thread.sleep(1000*1);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}

@Component
class KafkaProducer{

    //生产者发消息依靠一个工具，名为KafkaTemplate,其已被Spring整合入容器中，可以注入获得
    @Autowired(required = false)
    private KafkaTemplate kafkaTemplate;

    public void sendMessage(String topic, String content){
        kafkaTemplate.send(topic,content);
    }
}

@Component
class KafkaConsumer{
    //消费者不需要用到spring的相应模板，因其是被动的接受消息
    //只需要借助一个注解，名叫KafkaListener
    @KafkaListener(topics = {"jiashi2"})
    public void handleMessage(ConsumerRecord record){
        System.out.println(record.value());
    }
}
