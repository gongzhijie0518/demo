package com.leyou.user.web;

import com.leyou.common.exception.LyException;
import com.leyou.user.pojo.User;
import com.leyou.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

@RestController
public class UserController {
    @Autowired
    private UserService userService;

    /**
     * 校验数据是否唯一
     *
     * @param data
     * @param type
     * @return
     */
    @GetMapping("/check/{data}/{type}")
    public ResponseEntity<Boolean> checkData(@PathVariable("data") String data, @PathVariable("type") Integer type) {
        return ResponseEntity.ok(userService.checkData(data, type));
    }

    /**
     * 生成验证码
     *
     * @param phone
     * @return
     */
    @PostMapping("code")
    public ResponseEntity<Void> sendCode(@RequestParam("phone") String phone) {
        userService.sendCode(phone);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();

    }

    /**
     * 注册接口
     *
     * @param user
     * @param code
     */
    @PostMapping("register")
    public ResponseEntity<Void> register(@Valid User user, BindingResult result, @RequestParam("code") String code) {
        //判断校验成功还是失败
        if (result.hasErrors()) {
            List<FieldError> fieldErrors = result.getFieldErrors();
            String msg = fieldErrors.stream().map(FieldError::getDefaultMessage).
                    collect(Collectors.joining("|"));
            throw new LyException(400, msg);
        }

        userService.register(user, code);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    /**
     * 根据用户名和密码查询用户
     * @param username
     * @param password
     * @return
     */
    @GetMapping("query")
    public ResponseEntity<User> queryByUsernameAndPassword(
            @RequestParam ("username")String username, @RequestParam ("password")String password) {
        return ResponseEntity.ok(userService.queryByUsernameAndPassword(username, password));
    }
}
