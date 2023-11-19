package com.zhifu.community.event;

import com.alibaba.fastjson.JSONObject;
import com.qiniu.common.QiniuException;
import com.qiniu.common.Zone;
import com.qiniu.http.Response;
import com.qiniu.storage.Configuration;
import com.qiniu.storage.UploadManager;
import com.qiniu.util.Auth;
import com.qiniu.util.StringMap;
import com.zhifu.community.entity.DiscussPost;
import com.zhifu.community.entity.Event;
import com.zhifu.community.entity.Message;
import com.zhifu.community.service.DiscussPostService;
import com.zhifu.community.service.ElasticsearchService;
import com.zhifu.community.service.MessageService;
import com.zhifu.community.util.CommunityConstant;
import com.zhifu.community.util.CommunityUtil;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Future;

@Component
public class EventConsumer implements CommunityConstant {
    //处理事件的时候，需要记录日志
    private static final Logger logger = LoggerFactory.getLogger(EventConsumer.class);

    //处理事件时，也即插入数据到Message表中时需要用到MessageService，因此，需要提前注入
    @Autowired
    private MessageService messageService;

    @Autowired
    private DiscussPostService discussPostService;

    @Autowired
    private ElasticsearchService elasticsearchService;

    @Value("${wk.image.command}")
    private String wkImageCommand;

    @Value("${wk.image.storage}")
    private String wkImageStorage;

    @Value("${qiniu.key.access}")
    private String accessKey;

    @Value("${qiniu.key.secret}")
    private String secretKey;

    @Value("${qiniu.bucket.share.name}")
    private String shareBucketName;

    @Autowired(required = false)
    private ThreadPoolTaskScheduler taskScheduler;

    //接受消息：消费者处理事件
    //使用一个方法消费三个主题：点赞、评论、关注，（都是类似的event）
    @KafkaListener(topics = {TOPIC_COMMENT,TOPIC_LIKE,TOPIC_FOLLOW})
    //该方法需要添加一个参数来接受消息,该参数为ConsumerRecord
    public void handleCommentMessage(ConsumerRecord record){
        //判断内容是否有问题
        if(record == null || record.value() == null){
            logger.error("消息的内容为空！");
            return;
        }
        Event event = JSONObject.parseObject(record.value().toString(),Event.class);
        //判断格式是否有问题
        if(event == null){
            logger.error("消息格式错误！");
            return;
        }

        //发送站内系统通知
        Message message = new Message();
        message.setFromId(SYSTEM_USER_ID);
        message.setToId(event.getEntityUserId());
        message.setConversationId(event.getTopic());
        message.setCreateTime(new Date());

        Map<String,Object> content = new HashMap<>();
        content.put("userId",event.getUserId());
        content.put("entityType",event.getEntityType());
        content.put("entityId", event.getEntityId());

        if(!event.getData().isEmpty()){
            for(Map.Entry<String,Object> entry : event.getData().entrySet()){
                content.put(entry.getKey(),entry.getValue());
            }
        }

        message.setContent(JSONObject.toJSONString(content));
        messageService.addMessage(message);
    }


    //消费发帖事件
    @KafkaListener(topics = {TOPIC_PUBLISH})
    public void handlePublishMessage(ConsumerRecord record){
        if(record == null || record.value() == null){
            logger.error("消息的内容为空！");
            return;
        }

        Event event = JSONObject.parseObject(record.value().toString(),Event.class);
        if(event == null){
            logger.error("消息格式错误！");
            return;
        }
        DiscussPost discussPost = discussPostService.findDiscussPostById(event.getEntityId());
        elasticsearchService.saveDiscussPost(discussPost);

    }

    //消费删帖事件
    @KafkaListener(topics = {TOPIC_DELETE})
    public void handleDeleteMessage(ConsumerRecord record){
        if(record == null || record.value() == null){
            logger.error("消息的内容为空！");
            return;
        }

        Event event = JSONObject.parseObject(record.value().toString(),Event.class);
        if(event == null){
            logger.error("消息格式错误！");
            return;
        }
        elasticsearchService.deleteDiscussPost(event.getEntityId());

    }

    //消费分享事件
    @KafkaListener(topics = TOPIC_SHARE)
    public void handleShareMessage(ConsumerRecord record){
        if(record == null || record.value() == null){
            logger.error("消息的内容为空！");
            return;
        }

        Event event = JSONObject.parseObject(record.value().toString(),Event.class);
        if(event == null){
            logger.error("消息格式错误！");
            return;
        }

        String htmlUrl = (String) event.getData().get("htmlUrl");
        String fileName = (String) event.getData().get("fileName");
        String suffix = (String) event.getData().get("suffix");

        String cmd = wkImageCommand + " --quality 75 "
                + htmlUrl + " " + wkImageStorage + "/" + fileName + suffix;
        try {
            Runtime.getRuntime().exec(cmd);
            logger.info("生成长图成功：" + cmd);
        } catch (IOException e) {
            logger.error("生成长图失败：" + e.getMessage());
        }

        //启动定时器，监视该图片是否完成生成，一旦生成，则上传至七牛云
        UploadTask task = new UploadTask(fileName, suffix);
        //Future封装了执行任务Task的状态，并且可以用来停止任务。因此，需要将其传回task中用于执行任务的自动停止
        //future.cancel(true);表示终止定时任务的执行
        Future future = taskScheduler.scheduleAtFixedRate(task, 500);
        task.setFuture(future);

    }

    //设置Runnable的执行任务
    class UploadTask implements Runnable{
        //文件名称
        private String fileName;
        //文件后缀
        private String suffix;
        //长图的服务器本地文件路径
        private String path;
        //启动任务的返回值
        private Future future;
        //开始时间
        private long startTime;
        //上传次数
        private int uploadTimes;

        public UploadTask(String fileName, String suffix){
            this.fileName = fileName;
            this.suffix = suffix;
            this.path = wkImageStorage + "/" + fileName + suffix;
            this.startTime = System.currentTimeMillis();
        }

        public void setFuture(Future future) {
            this.future = future;
        }

        @Override
        public void run() {

            //生成失败
            if(System.currentTimeMillis() - startTime > 30000){
                logger.error("执行时间过长，终止任务：" + fileName);
                future.cancel(true);
                return;
            }
            //上传失败
            if(uploadTimes >= 3){
                logger.error("上传次数过多，终止任务：" + fileName);
                future.cancel(true);
                return;
            }

            File file = new File(path);
            if(file.exists()){
                logger.info(String.format("开始第%d次上传[%s].", ++uploadTimes,fileName));

                //上传步骤详情：------------------------------------------------------
                //1.设置响应信息
                StringMap policy = new StringMap();
                policy.put("returnBody", CommunityUtil.getJSONString(0));
                //2.生成上传凭证
                Auth auth = Auth.create(accessKey, secretKey);
                String uploadToken = auth.uploadToken(shareBucketName, fileName, 3600, policy);
                //3.指定上传机房
                UploadManager manager = new UploadManager(new Configuration(Zone.zone1()));
                //4.执行上传操作，根据响应结果作相应处理
                try{

                    //4.1 开始上传图片
                    Response response = manager.put(
                            path, fileName, uploadToken, null, "image/" + suffix, false);

                    //4.2 处理响应结果
                    JSONObject json = JSONObject.parseObject(response.bodyString());
                    if(json == null || json.get("code") == null || !json.get("code").toString().equals("0")){
                        logger.info(String.format("第%d次上传失败[%s].", uploadTimes, fileName));
                    }else {
                        logger.info(String.format("第%d次上传成功[%s].", uploadTimes, fileName));
                        future.cancel(true);
                    }

                }catch(QiniuException e){
                    logger.info(String.format("第%d次上传失败[%s].", uploadTimes, fileName));
                }
            }else {
                logger.info("等待图片生成[" + fileName + "].");
            }


        }
    }

}
