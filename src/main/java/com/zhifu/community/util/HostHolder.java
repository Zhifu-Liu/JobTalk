package com.zhifu.community.util;

import com.zhifu.community.entity.User;
import org.springframework.stereotype.Component;


/**
 * 持有用户的信息，用于代替session对象
 *  session可以直接持有用户数据，并且也是线程隔离的
 */
@Component
public class HostHolder {

    private ThreadLocal<User> users = new ThreadLocal<User>();

    public void setUser(User user){
        users.set(user);
    }

    public User getUser(){
        return users.get();
    }

    public void clear(){
        users.remove();
    }

}
