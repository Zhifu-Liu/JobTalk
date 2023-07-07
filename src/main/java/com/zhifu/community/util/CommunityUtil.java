package com.zhifu.community.util;

import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.DigestUtils;

import java.util.Map;
import java.util.UUID;

public class CommunityUtil {
    //生成随机字符串
    public static String generateUUID(){
        return UUID.randomUUID().toString().replaceAll("-","");
    }

    //MD5加密
    //hello -> abc123def456
    //hello + 3e4a8 -> abc123def456abc (加盐操作,提高安全性：salt,盐是随机生成的，黑客很难建立起字符串和密文的对应关系，也就无法破解加密，盐越长越复杂，破解难度就上一个等级)
    public static String md5(String key){
        if(StringUtils.isBlank(key)){
            return null;
        }
        //把传入的字符串key（key必须是byte格式）加密为16进制的字符串返回
        return DigestUtils.md5DigestAsHex(key.getBytes());
    }

    //生成JSON字符串
    public static String getJSONString(int code, String msg, Map<String,Object> map){
        JSONObject json = new JSONObject();
        json.put("code",code);
        json.put("msg",msg);
        if(map != null){
            for(String key : map.keySet()){
                json.put(key,map.get(key));
            }
        }
        return json.toJSONString();
    }

    //对生成JSON字符串方法 进行重载1
    public static String getJSONString(int code, String msg){
        return getJSONString(code,msg,null);
    }
    //对生成JSON字符串方法 进行重载2
    public static String getJSONString(int code){
        return getJSONString(code,null,null);
    }

}
