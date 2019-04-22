package com.leyou.sms.Utils;


import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;


@RunWith(SpringRunner.class)
@SpringBootTest
public class SmsUtilsTest {
    @Autowired
    private SmsUtils smsUtils;
     @Autowired
     private AmqpTemplate amqpTemplate;
    @Test
    public void smsUtilsTest() {
        smsUtils.sendMessage("13033711082", "个人体验使用", "SMS_151178415", "{\"code\":\"231214\"}");
    }
    @Test
    public void testSendMassage(){
        Map<String,String>map=new HashMap<>();
        map.put("phone","13033711082");
        map.put("code",String.valueOf(new Random().nextInt(10000)));
        amqpTemplate.convertAndSend("ly.sms.exchange","register.verify.code",map);
    }
}
