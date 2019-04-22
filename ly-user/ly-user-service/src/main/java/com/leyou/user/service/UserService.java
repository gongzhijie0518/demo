package com.leyou.user.service;

import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exception.LyException;
import com.leyou.common.utils.NumberUtils;
import com.leyou.user.mapper.UserMapper;
import com.leyou.user.pojo.User;
import com.leyou.user.utils.CodecUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class UserService {
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private AmqpTemplate amqpTemplate;
    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    private static final String KEY_PREFIX = "user:verify:phone";

    public Boolean checkData(String data, Integer type) {
        User user = new User();
        switch (type) {
            case 1:
                user.setUsername(data);
                break;
            case 2:
                user.setPhone(data);
                break;
            default:
                throw new LyException(ExceptionEnum.DATA_TYPE_ERROR);
        }
        int count = userMapper.selectCount(user);

        return count == 0;
    }

    public void sendCode(String phone) {
        //校验手机号
        String regex = "^1[35678]\\d{9}$";
        if (!phone.matches(regex)) {
            throw new LyException(ExceptionEnum.DATA_TYPE_ERROR);
        }
        //生成随机的6为数字
        String code = NumberUtils.generateCode(6);
        //把验证码发送到sms中心
        HashMap<String, String> map = new HashMap<>();
        map.put("phone", phone);
        map.put("code", code);
        amqpTemplate.convertAndSend("ly.sms.exchange", "register.verify.code", map);
        //把验证码存入redis
        stringRedisTemplate.opsForValue().set(KEY_PREFIX + phone, code, 5, TimeUnit.MINUTES);
    }


    public void register(User user, String code) {
        //TODO 校验用户数据
        //校验验证码
        String cacheCode = stringRedisTemplate.opsForValue().get(KEY_PREFIX + user.getPhone());
        if (!StringUtils.equals(cacheCode, code)) {
            //如果不一致直接返回错误
            throw new LyException(ExceptionEnum.DATA_TYPE_ERROR);
        }
        //对密码进行加密
        String salt = CodecUtils.generateSalt();
        user.setPassword(CodecUtils.md5Hex(user.getPassword(), salt));
        //存入数据库
        user.setCreated(new Date());
        user.setSalt(salt);
        userMapper.insert(user);
    }

    public User queryByUsernameAndPassword(String username, String password) {
        //根据用户名查询用户
        User u = new User();
        u.setUsername(username);
        User user = userMapper.selectOne(u);
        if (user == null) {
            //用户名不存在
            throw new LyException(ExceptionEnum.INVALID_USERNAME_PASSWORD);
        }
        //用户名不为空 获取盐 并根据输入的密码加密和 用户密码对比
        String salt = user.getSalt();
        String pw = CodecUtils.md5Hex(password, salt);
        if (!StringUtils.equals(user.getPassword(),pw)) {
            //密码错误
            throw new LyException(ExceptionEnum.INVALID_USERNAME_PASSWORD);
        }
        return user;
    }

    public static void main(String[] args) {
        String salt = CodecUtils.generateSalt();
        System.out.println(salt);
        String s = CodecUtils.md5Hex("123456", "aae0be5df12643cab2c25caa03ef5ad2");
        System.out.println(s);

    }
}
