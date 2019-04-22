package com.leyou.sms.Utils;

import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.IAcsClient;
import com.aliyuncs.dysmsapi.model.v20170525.SendSmsRequest;
import com.aliyuncs.dysmsapi.model.v20170525.SendSmsResponse;
import com.aliyuncs.http.MethodType;
import com.aliyuncs.profile.DefaultProfile;
import com.aliyuncs.profile.IClientProfile;
import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exception.LyException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
@Slf4j
@Component
public class SmsUtils {
    @Autowired
    private IAcsClient acsClient;

    public void sendMessage(String phone,String signName,String templateCode,String templateParams) {
        //设置超时时间-可自行调整

        try {    //组装请求对象
            SendSmsRequest request = new SendSmsRequest();
            //使用post提交
            request.setMethod(MethodType.POST);
            //必填:待发送手机号。支持以逗号分隔的形式进行批量调用，批量上限为1000个手机号码,批量调用相对于单条调用及时性稍有延迟,验证码类型的短信推荐使用单条调用的方式；发送国际/港澳台消息时，接收号码格式为国际区号+号码，如“85200000000”
            request.setPhoneNumbers(phone);
            //必填:短信签名-可在短信控制台中找到
            request.setSignName(signName);
            //必填:短信模板-可在短信控制台中找到，发送国际/港澳台消息时，请使用国际/港澳台短信模版
            request.setTemplateCode(templateCode);
            //可选:模板中的变量替换JSON串,如模板内容为"亲爱的${name},您的验证码为${code}"时,此处的值为
            //友情提示:如果JSON中需要带换行符,请参照标准的JSON协议对换行符的要求,比如短信内容中包含\r\n的情况在JSON中需要表示成\\r\\n,否则会导致JSON在服务端解析失败
            request.setTemplateParam(templateParams);

            //请求失败这里会抛ClientException异常
            SendSmsResponse sendSmsResponse = acsClient.getAcsResponse(request);
             if (sendSmsResponse==null||!"OK".equalsIgnoreCase(sendSmsResponse.getCode())){
                 log.error("【短信服务】发送短信失败，手机号码:{},错误原因:{}",phone,sendSmsResponse.getMessage());
                 throw  new LyException(ExceptionEnum.SEND_MESSAGE_ERROR);
             }
            log.info("【短信服务】发送短信成功！手机号:{}",phone);

        }catch (Exception e){
          log.error("【短信服务】发送短信失败，手机号码:{}",phone,e);
          throw  new LyException(ExceptionEnum.SEND_MESSAGE_ERROR);
        }
    }
}

