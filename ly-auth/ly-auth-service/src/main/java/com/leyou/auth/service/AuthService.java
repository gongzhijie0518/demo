package com.leyou.auth.service;

import com.leyou.auth.config.JwtProperties;
import com.leyou.auth.pojo.UserInfo;
import com.leyou.auth.utils.JwtUtils;
import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exception.LyException;

import com.leyou.user.api.UserClient;
import com.leyou.user.pojo.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
    @Autowired
    private UserClient userClient;
    @Autowired
    private JwtProperties prop;

    public String login(String username, String password) {
//        try {
            //校验用户名密码
            User user = userClient.queryByUsernameAndPassword(username, password);
            //生成token
            UserInfo userInfo = new UserInfo(user.getId(), user.getUsername());
            String token = JwtUtils.generateToken(userInfo, prop.getPrivateKey(), prop.getExpire());
            return token;
//        }catch (Exception e){
//           throw new LyException(ExceptionEnum.INVALID_USERNAME_PASSWORD);
//        }
    }
}
