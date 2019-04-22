package com.leyou.auth.web;

import com.leyou.auth.config.JwtProperties;
import com.leyou.auth.pojo.UserInfo;
import com.leyou.auth.service.AuthService;
import com.leyou.auth.utils.JwtUtils;
import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exception.LyException;
import com.leyou.common.utils.CookieUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@RestController
public class AuthController {
    @Autowired
    private AuthService authService;
    @Autowired
    private JwtProperties prop;

    /**
     * 登录
     *
     * @param username
     * @param password
     * @return
     */
    @PostMapping("login")
    public ResponseEntity<Void> login(
            @RequestParam("username") String username,
            @RequestParam("password") String password,
            HttpServletRequest request, HttpServletResponse response) {
       //校验用户名和密码 并生成jwt
        String token = authService.login(username, password);
      // 写到cookie中

        CookieUtils.newBuilder().name(prop.getCookName()).value(token)
                .request(request).response(response).httpOnly(true).build();
        return ResponseEntity.ok().build();

    }

    /**
     * 校验用户是否登录
     * @return
     */

    @GetMapping("verify")
    public ResponseEntity<UserInfo> verify(@CookieValue("LY_TOKEN")String token ,
                     HttpServletResponse response,HttpServletRequest request)  {
        //解析
        try {
            UserInfo userInfo = JwtUtils.getInfoFromToken(token, prop.getPublicKey());
            //刷新token
            token = JwtUtils.generateToken(userInfo, prop.getPrivateKey(), prop.getExpire());
            // 写到cookie中
            CookieUtils.newBuilder().name(prop.getCookName()).value(token)
                    .request(request).response(response).httpOnly(true).build();
            return  ResponseEntity.ok(userInfo);
        } catch (Exception e) {
            throw  new LyException(ExceptionEnum.UNAUTHORIZED);
        }

    }


}
