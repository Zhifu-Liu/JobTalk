package com.zhifu.community.controller;

import com.zhifu.community.entity.DiscussPost;
import com.zhifu.community.entity.Page;
import com.zhifu.community.service.ElasticsearchService;
import com.zhifu.community.service.LikeService;
import com.zhifu.community.service.UserService;
import com.zhifu.community.util.CommunityConstant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class SearchController  implements CommunityConstant {

    @Autowired
    private ElasticsearchService elasticsearchService;

    @Autowired
    private UserService userService;

    @Autowired
    private LikeService likeService;

    //GET方法提交参数的方式有两种，一个是路径中参量{}，一个是用？问号后带键值对
    //本次采用第二种方式
    // search?keyword=xxx
    @RequestMapping(path="/search",method = RequestMethod.GET)
    public String search(String keyword, Page page, Model model) throws IOException {
        //搜索帖子
        List<DiscussPost> resultList = elasticsearchService
                .searchDiscussPost(keyword,page.getCurrent()-1, page.getLimit());
        //聚合数据
        List<Map<String,Object>> discussPosts = new ArrayList<>();
        if(resultList != null){
            for (DiscussPost post: resultList){
                Map<String,Object> map = new HashMap<>();
                //帖子
                map.put("post",post);
                //帖子的作者
                map.put("user",userService.findUserById(post.getUserId()));
                //帖子的点赞数量
                map.put("likeCount",likeService.findEntityLikeCount(ENTITY_TYPE_POST,post.getId()));
                discussPosts.add(map);
            }
        }
        model.addAttribute("discussPosts",discussPosts);
        model.addAttribute("keyword",keyword);

        //设置分页信息
        page.setPath("/search?keyword=" + keyword);
        int rows = elasticsearchService.searchDiscussPostCount(keyword);
        page.setRows(rows);
        //page.setLimit(5);


        /*System.out.println(resultList ==null ? 0:resultList.size());
        System.out.println(rows);
        System.out.println(page.getLimit());*/

        return "/site/search";
    }
}
