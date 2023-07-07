package com.zhifu.community.event;

import com.alibaba.fastjson.JSONObject;
import com.zhifu.community.entity.Event;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class EventProducer {
    @Autowired(required = false)
    private KafkaTemplate kafkaTemplate;

    //发送消息,或者说生产者处理事件
    public void fireEvent(Event event){
        //将事件发布到指定的主题
        //JSONObject.toJSONString的作用是:将对象转换为JSON字符串
        kafkaTemplate.send(event.getTopic(), JSONObject.toJSONString(event));
    }
}
