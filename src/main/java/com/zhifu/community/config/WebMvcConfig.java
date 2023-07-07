package com.zhifu.community.config;


import com.zhifu.community.annotation.LoginRequired;
import com.zhifu.community.controller.interceptor.AlphaInterceptor;
import com.zhifu.community.controller.interceptor.LoginRequiredInterceptor;
import com.zhifu.community.controller.interceptor.LoginTicketInterceptor;
import com.zhifu.community.controller.interceptor.MessageInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;


/**
 * 拦截器的配置类不同于一般的配置类，不是为了简单的装配第三方的bean，而是要实现一个接口WebMvcConfigurer
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Autowired
    private AlphaInterceptor alphaInterceptor;

    @Autowired
    private LoginTicketInterceptor loginTicketInterceptor;

//      之前采用“加自定义注释”方式拦截器实现了登陆检查，这是简单的权限管理方案，现在将其废除
//    @Autowired
//    private LoginRequiredInterceptor loginRequiredInterceptor;

    @Autowired
    private MessageInterceptor messageInterceptor;

    @Override//重写：接口WebMvcConfigurer中的方法：注册接口方法
    public void addInterceptors(InterceptorRegistry registry) {

        //registry.addInterceptor(alphaInterceptor);如果单单是这一句，则拦截器会拦截一切请求
        //拦截器排除一些静态资源的访问拦截，静态资源的路径可以忽略域名和项目路径，“/**”表示static目录下所有的文件夹
        //还可以特意添加一些需要拦截的请求路径
        registry.addInterceptor(alphaInterceptor)
                .excludePathPatterns("/**/*.css","/**/*.js","/**/*.png","/**/*.jpg","/**/*.jpeg")
                .addPathPatterns("/register","/login");

        registry.addInterceptor(loginTicketInterceptor)
                .excludePathPatterns("/**/*.css","/**/*.js","/**/*.png","/**/*.jpg","/**/*.jpeg");

//        registry.addInterceptor(loginRequiredInterceptor)
//                .excludePathPatterns("/**/*.css","/**/*.js","/**/*.png","/**/*.jpg","/**/*.jpeg");

        registry.addInterceptor(messageInterceptor)
                .excludePathPatterns("/**/*.css","/**/*.js","/**/*.png","/**/*.jpg","/**/*.jpeg");

    }
}
