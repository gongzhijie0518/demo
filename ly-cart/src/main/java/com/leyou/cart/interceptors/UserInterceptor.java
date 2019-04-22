package com.leyou.cart.interceptors;

import com.leyou.auth.pojo.UserInfo;
import com.leyou.auth.utils.JwtUtils;
import com.leyou.cart.config.JwtProperties;
import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exception.LyException;
import com.leyou.common.utils.CookieUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class UserInterceptor implements HandlerInterceptor{

    private JwtProperties prop;

    public UserInterceptor(JwtProperties prop) {
        this.prop = prop;
    }

    private static final ThreadLocal<UserInfo> TL=new ThreadLocal<>();//线程域
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        try {
       //获取token
        String token = CookieUtils.getCookieValue(request, prop.getCookName());
        //解析token，获取用户
            UserInfo user = JwtUtils.getInfoFromToken(token, prop.getPublicKey());
            //把用户向后传递
//            request.setAttribute("userInfo",user);
            TL.set(user);
            return true;
        } catch (Exception e) {
            throw new LyException(ExceptionEnum.UNAUTHORIZED);
        }

    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        TL.remove();
    }

    public static UserInfo getUser(){
        return TL.get();
    }
}
