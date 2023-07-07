package com.zhifu.community.controller.interceptor;

import com.zhifu.community.entity.LoginTicket;
import com.zhifu.community.entity.User;
import com.zhifu.community.service.UserService;
import com.zhifu.community.util.CookieUtil;
import com.zhifu.community.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;


@Component
public class LoginTicketInterceptor implements HandlerInterceptor {

    @Autowired(required=false)
    private UserService userService;

    @Autowired
    private HostHolder hostHolder;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        //此方法来自于处理拦截器接口，接口已经对方法的规范进行了定义，因此，此方法的输入参数不能随便添加更改，也就无法使用注解@CookieValue来获取cookie
        //但是，可以通过request来得到cookie

        //从cookie中获取凭证
        String ticket = CookieUtil.getValue(request,"ticket");
        if(ticket != null){
            //根据登陆凭证，查询对应的登陆信息
            LoginTicket loginTciket = userService.findLoginTicket(ticket);
            //检查登陆信息是否有效
            if(loginTciket != null && loginTciket.getStatus() == 0 && loginTciket.getExpired().after(new Date())){
                User user = userService.findUserById(loginTciket.getUserId());
                // 在本次请求中持有该用户信息
                // 为了避免在服务器中出现多线程并发场景下的线程安全问题，我们需要将各线程的数据隔离起来，避免发生冲突。因此，采用TreadLocal
                //ThreadLocal替代Session的好处：可以在同一线程中很方便的获取用户信息，不需要频繁的传递session对象
                hostHolder.setUser(user);

                //构建用户认证的结果，并存入SecurityContext中，以便于Security进行授权。
                Authentication authentication = new UsernamePasswordAuthenticationToken(
                        user,user.getPassword(),userService.getAuthorities(user.getId()));
                SecurityContextHolder.setContext(new SecurityContextImpl(authentication));
            }
        }

        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {

        User user = hostHolder.getUser();
        if(user != null && modelAndView != null){
            modelAndView.addObject("loginUser",user);
        }
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {

        hostHolder.clear();
        //security做一个用户认证的权限清理
        //SecurityContextHolder.clearContext();
    }
}
