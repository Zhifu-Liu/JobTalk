package com.zhifu.community.controller;

import com.zhifu.community.annotation.LoginRequired;
import com.zhifu.community.entity.*;
import com.zhifu.community.event.EventProducer;
import com.zhifu.community.service.CommentService;
import com.zhifu.community.service.DiscussPostService;
import com.zhifu.community.service.LikeService;
import com.zhifu.community.service.UserService;
import com.zhifu.community.util.CommunityConstant;
import com.zhifu.community.util.CommunityUtil;
import com.zhifu.community.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.*;


@Controller
@RequestMapping(path="/discuss")
public class DiscussPostController implements CommunityConstant {
    @Autowired
    private LikeService likeService;

    @Autowired
    private CommentService commentService;

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private DiscussPostService discussPostService;

    @Autowired
    private UserService userService;

    @Autowired
    private EventProducer eventProducer;

    @RequestMapping(path="/add", method = RequestMethod.POST)
    @ResponseBody
    public String addDiscussPost( String title, String content){
        User user = hostHolder.getUser();

        //如果用户没登陆，因为是异步请求，就返回json格式的数据
        //403代表的是无权限访问

        if(user == null){
            return CommunityUtil.getJSONString(403,"你还没有登陆哦！");
        }
        DiscussPost post = new DiscussPost();
        post.setUserId(user.getId());
        post.setTitle(title);
        post.setContent(content);
        post.setCreateTime(new Date(System.currentTimeMillis()));
        discussPostService.addDiscussPost(post);

        //触发发帖事件
        Event event = new Event()
                .setTopic(TOPIC_PUBLISH)
                .setUserId(user.getId())
                .setEntityType(ENTITY_TYPE_POST)
                .setEntityId(post.getId());
        eventProducer.fireEvent(event);

        //上述步骤中，如果出现报错情况，将来统一处理，在本文件中先暂不考虑
        return CommunityUtil.getJSONString(0, "发布成功！");
    }


    @RequestMapping(path="/detail/{discussPostId}", method = RequestMethod.GET)
    public String getDiscussPost(@PathVariable("discussPostId") int discussPostId, Model model, Page page){
        //由于page的offset并不是一个成员变量，而是一个带有返回值的方法
        //因此，需要将方法返回结果直接插入到model中
        //model.addAttribute("")

        //只要是实体类型，javaBean，声明在函数输入条件中，作为一个参数的话，SpringMVC都会把它们存入model中
        //查询帖子
        DiscussPost post = discussPostService.findDiscussPostById(discussPostId);
        model.addAttribute("post",post);

        //根据用户ID，获取帖子的作者信息User。
        // 此处有两种实现方法：1.MyBatis支持在mapper处实现关联查询，一次数据库语句得到两张表；优缺点：效率较高，但是数据访问层的方法冗余，有较高耦合度
        // 2. 在本方法内进行User的查询，但访问效率会比较低，后期可以使用Redis进行优化；
        User user = userService.findUserById(post.getUserId());
        model.addAttribute("user",user);

        //获取当前登陆用户的信息
        User userLogin = hostHolder.getUser();
        //点赞数量
        long likeCount = likeService.findEntityLikeCount(ENTITY_TYPE_POST,discussPostId);
        model.addAttribute("likeCount",likeCount);
        //点赞状态
        int likeStatus =  userLogin == null ? 0 :
                likeService.findEntityLikeStatus(userLogin.getId(),ENTITY_TYPE_POST,discussPostId);
        //System.out.println(hostHolder.getUser() == null ? "当前未检测到用户":"检测到用户ID为:" + hostHolder.getUser().getId());
        //System.out.println(likeStatus);
        model.addAttribute("likeStatus",likeStatus);



        //查评论的分页信息
        page.setLimit(5);
        page.setPath("/discuss/detail/" + discussPostId);
        page.setRows(post.getCommentCount());

        // 评论：给帖子的评论
        // 回复：给评论的评论

        // 评论列表
        List<Comment> commentList = commentService.findCommentByEntity(
                ENTITY_TYPE_POST,post.getId(),page.getOffset(),page.getLimit());
        // 评论Vo列表
        List<Map<String, Object>> commentVoList = new ArrayList<>();
        if(commentList != null){
            for(Comment comment : commentList){
                //一个评论的VO
                Map<String , Object> commentVo = new HashMap<>();
                //1.评论
                commentVo.put("comment", comment);
                //2.作者
                commentVo.put("user",userService.findUserById(comment.getUserId()));

                //点赞数量
                likeCount = likeService.findEntityLikeCount(ENTITY_TYPE_COMMENT,comment.getId());
                commentVo.put("likeCount",likeCount);
                //点赞状态
                likeStatus =  userLogin == null ? 0 :
                        likeService.findEntityLikeStatus(userLogin.getId(),ENTITY_TYPE_COMMENT,comment.getId());
                commentVo.put("likeStatus",likeStatus);


                //回复列表
                List<Comment> replyList = commentService.findCommentByEntity(
                        ENTITY_TYPE_COMMENT,comment.getId(),0,Integer.MAX_VALUE);
                // 回复Vo列表
                List<Map<String, Object>> replyVoList = new ArrayList<>();
                if(replyList != null) {
                    for (Comment reply : replyList) {
                        //一个回复的VO
                        Map<String, Object> replyVo = new HashMap<>();
                        //1.回复
                        replyVo.put("reply", reply);
                        //2.作者
                        replyVo.put("user", userService.findUserById(reply.getUserId()));
                        //回复的目标
                        User target = reply.getTargetId() == 0 ? null : userService.findUserById(reply.getTargetId());
                        replyVo.put("target", target);
                        //点赞数量
                        likeCount = likeService.findEntityLikeCount(ENTITY_TYPE_COMMENT,reply.getId());
                        replyVo.put("likeCount",likeCount);
                        //点赞状态
                        likeStatus =  userLogin == null ? 0 :
                                likeService.findEntityLikeStatus(userLogin.getId(),ENTITY_TYPE_COMMENT,reply.getId());
                        replyVo.put("likeStatus",likeStatus);

                        replyVoList.add(replyVo);
                    }
                }
                commentVo.put("replys", replyVoList);

                // 回复数量
                int replyCount = commentService.findCountByEntity(ENTITY_TYPE_COMMENT, comment.getId());
                commentVo.put("replyCount", replyCount);


                commentVoList.add(commentVo);
            }
        }
        model.addAttribute("comments",commentVoList);
        return "/site/discuss-detail";
    }

    //置顶
    @RequestMapping(path = "/top" ,method = RequestMethod.POST)
    @ResponseBody
    public String setTop(int id){
        discussPostService.updateType(id, 1);

        //触发发帖事件
        Event event  = new Event()
                .setTopic(TOPIC_PUBLISH)
                .setUserId(hostHolder.getUser().getId())
                .setEntityType(ENTITY_TYPE_POST)
                .setEntityId(id);
        eventProducer.fireEvent(event);

        return CommunityUtil.getJSONString(0);
    }

    //加精
    @RequestMapping(path = "/wonderful" ,method = RequestMethod.POST)
    @ResponseBody
    public String setWonderful(int id){
        discussPostService.updateStatus(id, 1);

        //触发发帖事件
        Event event  = new Event()
                .setTopic(TOPIC_PUBLISH)
                .setUserId(hostHolder.getUser().getId())
                .setEntityType(ENTITY_TYPE_POST)
                .setEntityId(id);
        eventProducer.fireEvent(event);

        return CommunityUtil.getJSONString(0);
    }

    //删除
    @RequestMapping(path = "/delete" ,method = RequestMethod.POST)
    @ResponseBody
    public String setDelete(int id){
        discussPostService.updateStatus(id, 2);//设置为“拉黑”状态

        //触发删帖事件
        Event event  = new Event()
                .setTopic(TOPIC_DELETE)
                .setUserId(hostHolder.getUser().getId())
                .setEntityType(ENTITY_TYPE_POST)
                .setEntityId(id);
        eventProducer.fireEvent(event);

        return CommunityUtil.getJSONString(0);
    }

}
