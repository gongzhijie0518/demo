package com.leyou.user.pojo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import org.hibernate.validator.constraints.Length;
import tk.mybatis.mapper.annotation.KeySql;

import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.Pattern;
import java.util.Date;

@Table(name = "tb_user")
@Data
public class User {
    @Id
    @KeySql(useGeneratedKeys = true)
    private Long id;
    @Pattern(regexp = "^\\w{4,32}$",message ="用户名只能是4到32位" )
    private String username;// 用户名

    @JsonIgnore
    @Length(min = 4,max = 32,message ="密码只能是4到32位" )
    private String password;// 密码
    @Pattern(regexp = "^1[3456789]\\d{9}$",message ="手机号格式错误" )
    private String phone;// 电话

    private Date created;// 创建时间

    @JsonIgnore
    private String salt;// 密码的盐值
}