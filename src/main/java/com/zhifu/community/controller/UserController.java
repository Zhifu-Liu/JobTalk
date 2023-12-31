package com.zhifu.community.controller;


import com.qiniu.util.Auth;
import com.qiniu.util.StringMap;
import com.zhifu.community.annotation.LoginRequired;
import com.zhifu.community.entity.User;
import com.zhifu.community.service.FollowService;
import com.zhifu.community.service.LikeService;
import com.zhifu.community.service.UserService;
import com.zhifu.community.util.CommunityConstant;
import com.zhifu.community.util.CommunityUtil;
import com.zhifu.community.util.HostHolder;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;
import java.util.Objects;


@Controller
@RequestMapping(path="/user")
public class UserController implements CommunityConstant {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @Value("${community.path.upload}")
    private String uploadPath;

    @Value("${community.path.domin}")
    private String domin;

    @Value("${server.servlet.context-path}")
    private String contextPath;

    @Value("${qiniu.key.access}")
    private String accessKey;

    @Value("${qiniu.key.secret}")
    private String secretKey;

    @Value("${qiniu.bucket.header.name}")
    private String headerBucketName;

    @Value("${qiniu.bucket.header.url}")
    private String headerBucketUrl;

    @Autowired
    private UserService userService;

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private LikeService likeService;

    @Autowired
    private FollowService followService;

    //由于将头像存储位置：服务器本地 -> 七牛云存储,而七牛云上传文件时需要上传token
    //因此，需要在设置头像的请求方法中，将上传token提前设置好，并放在setting页面的model中
    @LoginRequired
    @RequestMapping(path="/setting",method = RequestMethod.GET)
    public String getSettingPage(Model model){
        //上传文件名称
        String fileName = CommunityUtil.generateUUID();
        //设置处理成功后的返回响应信息
        StringMap policy = new StringMap();
        policy.put("returnBody", CommunityUtil.getJSONString(0));
        //生成上传凭证token
        Auth auth = Auth.create(accessKey, secretKey);
        String uploadToken = auth.uploadToken(headerBucketName,fileName,3600,policy);

        model.addAttribute("uploadToken", uploadToken);
        model.addAttribute("fileName", fileName);

        return "/site/setting";
    }

    //更新用户头像路径
    @RequestMapping(path = "/header/url", method = RequestMethod.POST)
    @ResponseBody
    public String updateHeaderUrl(String fileName){
        if(StringUtils.isBlank(fileName)){
            return CommunityUtil.getJSONString(1,"文件名不能为空！");
        }

        String url = headerBucketUrl + "/" + fileName;
        userService.updateHeader(hostHolder.getUser().getId(), url);

        return CommunityUtil.getJSONString(0);
    }


    //原有方法：头像上传到服务器本地，该方法需要废弃掉
    //更改后方法：七牛云实现上传接口，无需在服务器上作实现
    @LoginRequired
    @RequestMapping(path="/upload",method=RequestMethod.POST)
    public String uploadHeader(MultipartFile headerImage, Model model){
        if(headerImage == null){
            model.addAttribute("error","您还没有选择图片！");
            return "/site/setting";
        }

        String fileName = headerImage.getOriginalFilename();//得到图片的全称
        String suffix = fileName.substring(fileName.lastIndexOf("."));
        if(StringUtils.isBlank(suffix)){
            model.addAttribute("error","文件的格式不正确！");
            return "/site/setting";
        }

        //生成随机文件名
        fileName = CommunityUtil.generateUUID() + suffix;
        //确定文件存放的路径
        File dest = new File(uploadPath + "/" + fileName);
        try {
            //存储文件
            headerImage.transferTo(dest);
        } catch (IOException e) {
            logger.error("上传文件失败："+e.getMessage());
            throw new RuntimeException("上传文件失败，服务器发生异常！",e);
        }

        //更新当前用户的头像路径（web访问路径）
        //http://localhost:8088/community/user/header/xxx.png
        User user = hostHolder.getUser();
        String headerUrl = domin + contextPath + "/user/header/" + fileName;
        userService.updateHeader(user.getId(),headerUrl);
        return "redirect:/index";

    }

    //原有方法：头像从服务器本地获取，该方法需要废弃掉
    //更改后方法：七牛云实现下载接口，无需在服务器上作实现
    @RequestMapping(path="/header/{fileName}",method=RequestMethod.GET)
    public void getHeader(@PathVariable("fileName") String fileName, HttpServletResponse response){
        //服务器存放的路径
        fileName = uploadPath + "/" + fileName;
        //文件的后缀
        String suffix = fileName.substring(fileName.lastIndexOf("."));
        //设置响应图片格式
        response.setContentType("image/" + suffix);
        try(
                FileInputStream fis = new FileInputStream(fileName);
                ) {
            OutputStream os = response.getOutputStream();
            //该输出流是由response创建的，其关闭有Spring容器管理
            //该输入流由用户自己创建，因此，其关闭也需要用户来操作实现
            byte[] buffer = new byte[1024];
            int b = 0;//需要一个游标
            while((b = fis.read(buffer)) != -1){
                os.write(buffer,0,b);
            }
        } catch (IOException e) {
            logger.error("读取头像失败：" + e.getMessage());
        }
    }

    @LoginRequired
    @RequestMapping(path="/password",method=RequestMethod.POST)
    public String  updatePassword(String oldPassword, String newPassword, String confirmPassword,
                                  Model model){
        //空值验证
        if(StringUtils.isBlank(oldPassword)){
            model.addAttribute("oldPasswordMsg","原始密码输入不能为空！");
            return "/site/setting";
        }
        if(StringUtils.isBlank(newPassword)){
            model.addAttribute("newPasswordMsg","新密码输入不能为空！");
            return "/site/setting";
        }
        if(StringUtils.isBlank(confirmPassword)){
            model.addAttribute("confirmPasswordMsg","确认密码输入不能为空！");
            return "/site/setting";
        }

        //此时，输入均不为空值
        //默认旧密码输入正确，其与数据库中的密码的比较放在业务层

        //验证新密码是否不同于旧密码
        if(oldPassword.equals(newPassword)){
            model.addAttribute("newPasswordMsg","新密码和旧密码相等，请输入不同于旧密码的新密码！");
            return "/site/setting";
        }
        //验证确认密码与新密码的一致性
        if(!newPassword.equals(confirmPassword)){
            model.addAttribute("confirmPasswordMsg","确认密码与新密码不一致，请再次确认新密码！");
            return "/site/setting";
        }
        User user = hostHolder.getUser();
        Map<String,Object> map = userService.updatePassword(user,oldPassword,newPassword);
        if(map.containsKey("oldPasswordMsg")){
            model.addAttribute("oldPasswordMsg",map.get("oldPasswordMsg"));
            return "/site/setting";
        }else{
            return "redirect:/logout";
        }
    }

    //个人主页
    @RequestMapping(path="/profile/{userId}",method=RequestMethod.GET)
    public String getProfilePage(@PathVariable("userId") int userId, Model model){
        User user = userService.findUserById(userId);
        //为了防止传入大量错误id，导致服务器受到dos攻击，因此，先做检查
        if(user ==null){
            throw new RuntimeException("该用户不存在！");
        }

        //用户存在的情况下
        model.addAttribute("user",user);
        //用户的被点赞数量
        int likeCount = likeService.findUserLikeCount(userId);
        model.addAttribute("likeCount",likeCount);

        //关注数量
        long followeeCount = followService.findFolloweeCount(userId,ENTITY_TYPE_USER);
        model.addAttribute("followeeCount",followeeCount);
        //粉丝数量
        long followerCount = followService.findFollowerCount(ENTITY_TYPE_USER,userId);
        model.addAttribute("followerCount",followerCount);
        //是否已关注
        boolean hasFollowed =false;
        if(hostHolder.getUser() != null){
            hasFollowed = followService.hasFollowed(hostHolder.getUser().getId(),ENTITY_TYPE_USER,userId);
        }
        model.addAttribute("hasFollowed",hasFollowed);

        return "/site/profile";
    }

}
