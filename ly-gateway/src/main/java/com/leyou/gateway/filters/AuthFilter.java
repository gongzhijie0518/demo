package com.leyou.gateway.filters;

import com.leyou.auth.pojo.UserInfo;
import com.leyou.auth.utils.JwtUtils;
import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exception.LyException;
import com.leyou.common.utils.CookieUtils;
import com.leyou.gateway.config.FilterProperties;
import com.leyou.gateway.config.JwtProperties;
import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import com.netflix.zuul.exception.ZuulException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.protocol.RequestContent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.netflix.zuul.filters.support.FilterConstants;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@Slf4j
@Component
public class AuthFilter extends ZuulFilter {
    @Autowired
    private JwtProperties prop;
    @Autowired
    private FilterProperties filterProp;

    //过滤器类型
    @Override
    public String filterType() {
        return FilterConstants.PRE_TYPE;
    }

    //过滤器顺序
    @Override
    public int filterOrder() {
        return FilterConstants.FORM_BODY_WRAPPER_FILTER_ORDER - 1;
    }

    /**
     * 过滤器是否生效
     * @return ture 生效  被拦截
     */
    @Override
    public boolean shouldFilter() {
        //获取context
        RequestContext ctx = RequestContext.getCurrentContext();
        //获取request
        HttpServletRequest request = ctx.getRequest();
        //获取请求路径
        String path = request.getRequestURI();
        //判断当前请求路径是否需要放行
       boolean isAllow=isAllow(path);
        //放行返回false

        return !isAllow;
    }
    private boolean isAllow(String path) {
        List<String> allowPaths = filterProp.getAllowPaths();
        for (String allowPath : allowPaths) {
            if (StringUtils.startsWith(path,allowPath)){
                return true;
            }
        }
        return false;
    }

    //guolv逻辑
    @Override
    public Object run() throws ZuulException {
        //1、获取上下文对象及request对象
        RequestContext ctx = RequestContext.getCurrentContext();
        //2、获取cookie中的token
        HttpServletRequest request = ctx.getRequest();
        String token = CookieUtils.getCookieValue(request, prop.getCookName());
        //3、解析token 获取用户

        try {
            UserInfo user = JwtUtils.getInfoFromToken(token, prop.getPublicKey());
        } catch (Exception e) {
            //4、解析失败说明登录无效，拦截
            ctx.setSendZuulResponse(false);
            //401状态码表示未授权
            ctx.setResponseStatusCode(401);
            log.error("【网关访问】，用户没有访问权限!!!", e);
        }
        //TODO 用户权限校验
        //5、登录成功说明有效（鉴权后）放行
        return null;
    }
}
