package com.zhifu.community.controller;

import com.zhifu.community.annotation.LoginRequired;
import com.zhifu.community.entity.Event;
import com.zhifu.community.entity.User;
import com.zhifu.community.event.EventProducer;
import com.zhifu.community.service.LikeService;
import com.zhifu.community.util.CommunityConstant;
import com.zhifu.community.util.CommunityUtil;
import com.zhifu.community.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.Map;

@Controller
public class LikeController implements CommunityConstant {
    @Autowired
    private LikeService likeService;

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private EventProducer eventProducer;

    @LoginRequired
    @RequestMapping(path="/like",method = RequestMethod.POST)
    @ResponseBody
    public String like(int entityType ,int entityId, int entityUserId,int postId){
        User user = hostHolder.getUser();

        //实现点赞
        likeService.like(user.getId(),entityType,entityId,entityUserId);
        //获取点赞数量
        long likeCount = likeService.findEntityLikeCount(entityType,entityId);
        //获取点赞后状态
        int likeStatus = likeService.findEntityLikeStatus(user.getId(),entityType,entityId);

        //将结果用Map进行打包返回
        Map<String, Object> map = new HashMap<>();
        map.put("likeCount",likeCount);
        map.put("likeStatus",likeStatus);

        //触发点赞事件
        //由于可能是‘点赞’或者‘取消点赞’,我们只对‘点赞’事件，作消息队列存入
        if(likeStatus == 1){
            Event event = new Event()
                    .setTopic(TOPIC_LIKE)
                    .setUserId(user.getId())
                    .setEntityType(entityType)
                    .setEntityId(entityId)
                    .setEntityUserId(entityUserId)
                    .setData("postId",postId);
            eventProducer.fireEvent(event);
        }

        return CommunityUtil.getJSONString(0,null,map);
    }
}
