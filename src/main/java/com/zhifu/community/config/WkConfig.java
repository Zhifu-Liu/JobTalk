package com.zhifu.community.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.io.File;

@Configuration
public class WkConfig {
    private static final Logger logger = LoggerFactory.getLogger(WkConfig.class);

    @Value("${wk.image.storage}")
    private String wkImageStorage;
    @Value("${wk.pdf.storage}")
    private String wkPdfStorage;

    @PostConstruct
    public void init(){
        //创建WK图片目录
        File wkImageFile = new File(wkImageStorage);
        File wkPdfFile = new File(wkPdfStorage);
        if(!wkImageFile.exists()){
            wkImageFile.mkdir();
            logger.info("创建Wk图片目录：" + wkImageStorage);
        }
        if(!wkPdfFile.exists()){
            wkPdfFile.mkdir();
            logger.info("创建Wk图片目录：" + wkPdfStorage);
        }
    }
}
