package com.zhifu.community.controller;

import com.zhifu.community.dao.DiscussPostMapper;
import com.zhifu.community.dao.UserMapper;
import com.zhifu.community.entity.DiscussPost;
import com.zhifu.community.entity.Page;
import com.zhifu.community.entity.User;
import com.zhifu.community.service.DiscussPostService;
import com.zhifu.community.service.LikeService;
import com.zhifu.community.service.UserService;
import com.zhifu.community.util.CommunityConstant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class HomeController implements CommunityConstant {
    @Autowired(required=false)
    private DiscussPostService discussPostService;

    @Autowired(required=false)
    private UserService userService;

    @Autowired
    private LikeService likeService;

    @RequestMapping(path="/index", method= RequestMethod.GET)
    public String getIndexPage(Model model, Page page,
                               @RequestParam(name = "orderMode", defaultValue = "0") int orderMode){
        //orderMode的@RequestParam注解，是通过url的？后的参数值得来的，而不是post的请求体参数
        //后续的page分页信息也需要用到orderMode的信息，因此，需要对page的path添加orderMode的信息

        //方法调用前，SpringMVC会自动实例化Model和Page，并将Page注入到Model。
        //所以，在Thymeleaf中可以直接访问Page对象中的数据。
        page.setRows(discussPostService.findDiscussPostRows(0));
        page.setPath("/index?orderMode=" + orderMode);

        List<DiscussPost> list = discussPostService
                .findDiscussPosts(0,page.getOffset(),page.getLimit(), orderMode);
        List<Map<String,Object>> discussPosts = new ArrayList<>();
        if(list != null){
            for(DiscussPost post: list){
                Map<String,Object> map = new HashMap<>();
                map.put("post",post);
                User user = userService.findUserById(post.getUserId());
                map.put("user",user);

                long likeCount = likeService.findEntityLikeCount(ENTITY_TYPE_POST,post.getId());
                map.put("likeCount",likeCount);

                discussPosts.add(map);

            }
        }
        model.addAttribute("discussPosts",discussPosts);
        model.addAttribute("orderMode", orderMode);
        return "/index";
    }

    @RequestMapping(path = "denied", method = RequestMethod.GET)
    public String getDeniedPage(){
        return "/error/404";
    }

}
