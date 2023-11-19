package com.zhifu.community.controller;

import com.zhifu.community.config.WkConfig;
import com.zhifu.community.entity.Event;
import com.zhifu.community.event.EventProducer;
import com.zhifu.community.util.CommunityConstant;
import com.zhifu.community.util.CommunityUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

@Controller
public class ShareController implements CommunityConstant{
    private static final Logger logger = LoggerFactory.getLogger(WkConfig.class);

    @Autowired
    private EventProducer eventProducer;

    @Value("${community.path.domin}")
    private String domin;

    @Value("${server.servlet.context-path}")
    private String contextPath;

    @Value("${wk.image.storage}")
    private String wkImageStorage;

    @Value("${qiniu.bucket.share.url}")
    private String shareBucketUrl;

    // 通过"？"传入一个网址并将生成长图任务给kafka，并返回长图访问链接
    //Before: 长图存放在服务器本地
    //After:  长图存放在七牛云上
    @RequestMapping(path = "/share", method = RequestMethod.GET)
    @ResponseBody
    public String share(String htmlUrl){
        //随机生成一个文件名
        String fileName = CommunityUtil.generateUUID();

        //构建相关事件Event,异步生成长图
        Event event = new Event()
                .setTopic(TOPIC_SHARE)
                .setData("htmlUrl", htmlUrl)
                .setData("fileName", fileName)
                .setData("suffix",".png");
        eventProducer.fireEvent(event);

        //返回访问路径
        Map<String, Object> map = new HashMap<>();
        //map.put("shareUrl",domin + contextPath + "/share/image/" + fileName);
        map.put("shareUrl", shareBucketUrl + "/" + fileName);

        return CommunityUtil.getJSONString(0,null,map);
    }

    //获取长图，已废弃
    //Before: 长图存放在服务器本地时，需要在服务器的controller中实现获取长截图的接口
    //After:  长图存放在七牛云上后，七牛云有对应的获取长截图的接口，因此，如下接口废弃，不再采用
    @RequestMapping(path = "/share/image/{fileName}", method = RequestMethod.GET)
    public void getShareImage(@PathVariable("fileName") String fileName, HttpServletResponse response){
        if(StringUtils.isBlank(fileName)){
            throw new IllegalArgumentException("文件名不能为空！");
        }

        response.setContentType("image/png");
        File file = new File(wkImageStorage + "/" + fileName + ".png");
        try {
            //将服务器本地的图片作http输出时的流程如下：
            //本地文件file -> 文件输入流fis -> fis.read(buffer)读取数据到缓冲区buffer
            // -> os.write(buffer,0,b)将缓冲区的内容写入到输出流os中

            //获取了 HTTP 响应的输出流，用于将文件内容写入响应
            OutputStream os = response.getOutputStream();
            //创建了一个文件输入流（FileInputStream），用于从指定的文件（file）中读取数据。
            FileInputStream fis = new FileInputStream(file);
            //创建了一个字节数组作为读取时的缓冲区。缓冲区的大小为 1024 字节，意味着每次最多读取 1024 字节的数据。
            byte[] buffer = new byte[1024];
            //声明并初始化了一个整型变量 b，用作读取时的游标。初始值为 0。
            int b = 0;
            //迭代从文件输入流中读取数据到缓冲区，并检查是否读到文件末尾（返回值为 -1）
            while( (b = fis.read(buffer)) != -1 ){
                //将缓冲区中的数据通过输出流写入到 HTTP 响应中。
                // buffer 是源字节数组，0 是写入数据的起始位置，b 是要写入的字节数（实际读取的字节数）。
                os.write(buffer,0,b);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
