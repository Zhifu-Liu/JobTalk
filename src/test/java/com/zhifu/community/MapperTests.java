package com.zhifu.community;


import com.zhifu.community.dao.DiscussPostMapper;
import com.zhifu.community.dao.LoginTicketMapper;
import com.zhifu.community.dao.MessageMapper;
import com.zhifu.community.dao.UserMapper;
import com.zhifu.community.entity.DiscussPost;
import com.zhifu.community.entity.LoginTicket;
import com.zhifu.community.entity.Message;
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
    @Autowired(required = false)
    private MessageMapper messageMapper;

    @Autowired(required = false)
    private LoginTicketMapper loginTicketMapper;

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

    @Test
    public void testInsertLoginTicket(){
        LoginTicket loginTicket = new LoginTicket();
        loginTicket.setUserId(101);
        loginTicket.setTicket("abc");
        loginTicket.setStatus(0);
        loginTicket.setExpired(new Date(System.currentTimeMillis() + 1000 * 60 * 10));
        loginTicketMapper.insertLoginTicket(loginTicket);
    }

    @Test
    public void testSelectLoginTicket(){
        LoginTicket loginTicket = loginTicketMapper.selectByTicket("abc");
        System.out.println(loginTicket);

        loginTicketMapper.updateStatus("abc",1);
        loginTicket = loginTicketMapper.selectByTicket("abc");
        System.out.println(loginTicket);
    }

    @Test
    public void testSelectLetters(){
        List<Message> list = messageMapper.selectConversations(111,0,20);
        for(Message message:list){
            System.out.println(message);
        }
        int count = messageMapper.selectConversationCount(111);
        System.out.println(count);

        list = messageMapper.selectLetters("111_112",0,10);
        for(Message message:list){
            System.out.println(message);
        }
        count = messageMapper.selectLetterCount("111_112");
        System.out.println(count);

        count = messageMapper.selectLetterUnreadCount(131,"111_131");
        System.out.println(count);
    }
}
