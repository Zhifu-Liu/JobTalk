package com.zhifu.community;


import com.zhifu.community.dao.DiscussPostMapper;
import com.zhifu.community.dao.UserMapper;
import com.zhifu.community.entity.DiscussPost;
import com.zhifu.community.entity.User;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.sql.Date;
import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)
public class MapperTests {

    @Autowired(required=false)
    private UserMapper userMapper;

    @Autowired(required=false)
    private DiscussPostMapper discussPostMapper;

    @Test
    public void testSelectUser(){
        User user = userMapper.selectById(101);
        System.out.println(user);

        user = userMapper.selectByName("liubei");
        System.out.println(user);

        user = userMapper.selectByEmail("nowcoder101@sina.com");
        System.out.println(user);
    }

    @Test
    public void testInsertUser(){
        User user = new User();
        user.setUsername("liuzhifu");
        user.setPassword("123456");
        user.setSalt("abc");
        user.setEmail("2898118638@qq.com");
        user.setHeaderUrl("http://www.nowcoder.com/101.png");
        user.setActivationCode("activation123");
        user.setStatus(1);
        user.setType(2);
        long millis = System.currentTimeMillis();
        //System.out.println("当前时间戳为："+millis);
        user.setCreateTime(new Date(millis));
        int rows = userMapper.insertUser(user);
        System.out.println(rows);
        System.out.println(user.getId());
    }

    @Test
    public void testUpdateUser(){
        int rows = userMapper.updateHeader(151,"http://www.nowcoder.com/152.png");
        System.out.println(rows);

        rows = userMapper.updatePassword(152,"idontknow");
        System.out.println(rows);

        rows = userMapper.updateStatus(153,0);
        System.out.println(rows);
    }

    @Test
    public void testSelectPosts(){

        List<DiscussPost> list = discussPostMapper.selectDiscussPosts(149,0,10);
        for(DiscussPost discussPost:list){
            System.out.println(discussPost);
        }

        int rows = discussPostMapper.selectDiscussPostRows(149);
        System.out.println(rows);

        //discussPostMapper.selectDiscussPosts()
    }
}
