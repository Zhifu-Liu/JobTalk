package com.zhifu.community.config;

import com.zhifu.community.util.CommunityConstant;
import com.zhifu.community.util.CommunityUtil;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

@Configuration
public class SecurityConfig extends WebSecurityConfigurerAdapter implements CommunityConstant {

    //本方法主要为了忽略掉对静态资源的访问拦截
    @Override
    public void configure(WebSecurity web) throws Exception {
        web.ignoring().antMatchers("/resources/**");
    }

    //由于登陆、退出等相关功能已经实现，因此暂不配置认证方法。之后会绕过该方法
    /*@Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        super.configure(auth);
    }*/


    //配置授权方法: 什么身份可以访问什么东西
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.authorizeRequests()
                .antMatchers(//配置可以访问的资源
                        "/user/setting",
                        "/user/upload",
                        "/discuss/add",
                        "/comment/add/**",
                        "/letter/**",
                        "/notice/**",
                        "/like",
                        "/follow",
                        "/unfollow"

                ).hasAnyAuthority(//配置可以访问的人的身份权限
                        AUTHORITY_USER,
                        AUTHORITY_ADMIN,
                        AUTHORITY_MODERATOR
        )
                .antMatchers(
                        "/discuss/top",
                        "/discuss/wonderful"
                ).hasAnyAuthority(
                        AUTHORITY_MODERATOR
        )
                .antMatchers(
                        "/discuss/delete",
                        "/data/**",
                        "/actuator/**"
                ).hasAnyAuthority(
                        AUTHORITY_ADMIN
        )
                .anyRequest().permitAll()//除了上述请求外，其他任何请求都允许访问，不设置访问权限
                .and().csrf().disable();//关闭csrf的token验证功能

        //当权限不够时的处理
        http.exceptionHandling()
                //.accessDeniedPage() :单纯返回一个错误页面，只适用于同步请求，对异步请求不适用。因此，采用如下方法
                .authenticationEntryPoint(new AuthenticationEntryPoint() {
                    //没有登陆时，如何处理
                    @Override
                    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException, ServletException {
                        String xRequestedWith = request.getHeader("x-requested-with");
                        if("XMLHttpRequest".equals(xRequestedWith)){
                            //异步请求
                            response.setContentType("application/plain;charset=utf-8");
                            PrintWriter writer = response.getWriter();
                            writer.write(CommunityUtil.getJSONString(403,"你还没有登录哦！"));
                        }else {
                            response.sendRedirect(request.getContextPath() + "/login");
                        }
                    }
                })
                .accessDeniedHandler(new AccessDeniedHandler() {
                    //登陆了，但是权限不够，如何处理
                    @Override
                    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException) throws IOException, ServletException {
                        String xRequestedWith = request.getHeader("x-requested-with");
                        if("XMLHttpRequest".equals(xRequestedWith)){
                            //异步请求
                            response.setContentType("application/plain;charset=utf-8");
                            PrintWriter writer = response.getWriter();
                            writer.write(CommunityUtil.getJSONString(403,"你没有访问此功能的权限！"));
                        }else {
                            response.sendRedirect(request.getContextPath() + "/denied");
                        }
                    }
                });
        //  Security底层默认会拦截/logout请求，进行退出处理
        // 覆盖它默认的逻辑，才能执行我们自己的退出代码.
        // 通过设置logoutUrl()方法，改变security默认拦截的退出请求“/logout”，将其改为一个不存在的请求，使其失效
        http.logout().logoutUrl("securitylogout");
    }
}
