package com.zhifu.community.controller.interceptor;

import com.zhifu.community.entity.User;
import com.zhifu.community.service.DataService;
import com.zhifu.community.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
public class DataInterceptor implements HandlerInterceptor {
    @Autowired
    private DataService dataService;

    @Autowired
    private HostHolder hostHolder;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        //统计UV
        //如果请求段输入的为localHost，则得到的IP可能为0.0.0.1
        String ip = request.getRemoteHost();
        dataService.recordUV(ip);

        //统计DAU
        User user = hostHolder.getUser();
        if(user != null){
            dataService.recordDAU(user.getId());
        }

        return true;
    }
}
