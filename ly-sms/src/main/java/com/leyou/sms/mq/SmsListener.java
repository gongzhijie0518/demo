package com.leyou.sms.mq;

import com.leyou.common.utils.JsonUtils;
import com.leyou.sms.Utils.SmsUtils;
import com.leyou.sms.config.SmsProperties;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class SmsListener {
    @Autowired
    private SmsUtils smsUtils;
    @Autowired
    private SmsProperties properties;
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(name = "sms.verify.code.queue", durable = "true"),
            exchange = @Exchange(name = "ly.sms.exchange", type = ExchangeTypes.TOPIC),
            key = "register.verify.code"
    ))
    public void listenVerifyCode(Map<String, String> msg) {
        if (msg != null && msg.containsKey("phone")) {
            String phone = msg.remove("phone");
            if (phone.matches("^1[35789]\\d{9}$")) {
                smsUtils.sendMessage(phone, properties.getSignName(), properties.getVerifyTemplateCode(), JsonUtils.toString(msg));
            }
        }

    }
}
