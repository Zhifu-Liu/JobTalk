package com.zhifu.community.controller.interceptor;

import com.zhifu.community.annotation.LoginRequired;
import com.zhifu.community.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;

@Component
public class LoginRequiredInterceptor implements HandlerInterceptor {

    @Autowired
    private HostHolder hostHolder;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        //Object handler是我们拦截的目标，我们首先需要判断一下我们拦截的目标是否为方法（因为，拦截器可能拦截一起请求，包括静态资源）
        //HandlerMethod是SpringMVC提供的一个类型，代表了handler拦截的是方法
        if(handler instanceof HandlerMethod){
            HandlerMethod handlerMethod = (HandlerMethod) handler;//类型转换
            Method method = handlerMethod.getMethod();//获取拦截对象中的方法对象
            LoginRequired loginRequired = method.getDeclaredAnnotation(LoginRequired.class);//利用反射机制，获取目标注解对象

            //判断：当前方法需要登陆，但是检测不到登陆用户
            if(loginRequired != null && hostHolder.getUser() == null){
                response.sendRedirect(request.getContextPath() + "/login");//重定向的底层实现方式
                return false;
            }
        }
        return true;
    }
}
