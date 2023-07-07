package com.zhifu.community.controller.interceptor;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


@Component
public class AlphaInterceptor implements HandlerInterceptor {//实现处理拦截器handlerInterceptor接口，重写默认方法，进行controller之前的请求拦截
    private static final Logger logger = LoggerFactory.getLogger(AlphaInterceptor.class);

    //在Controller之前执行
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        logger.debug("preHandle: "+handler.toString());//日志起到调试作用，因此只需将其设置为debug模式即可
        return true;//该方法返回true，则controller执行；否则controller不执行。一般都返回true，选择接着执行controller
    }

    //在controller之后执行，view层模板引擎渲染之前，因此，有ModelAndView来承载数据
    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {

        logger.debug("postHandle: "+handler.toString());
    }

    //在view层模板引擎渲染之后执行
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {

        logger.debug("afterCompletion: " + handler.toString());
    }
}
