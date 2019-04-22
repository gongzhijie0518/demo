package com.leyou.sms.config;

import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.IAcsClient;
import com.aliyuncs.profile.DefaultProfile;
import com.aliyuncs.profile.IClientProfile;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SmsConfig {


    @Bean
    public IAcsClient iAcsClient(SmsProperties prop) throws Exception {
        System.setProperty("sun.net.client.defaultConnectTimeout", prop.getConnectTimeout());
        System.setProperty("sun.net.client.defaultReadTimeout", prop.getReadTimeout());
        //初始化ascClient需要的几个参数
        final String product = "Dysmsapi";//短信API产品名称（短信产品名固定，无需修改）
        final String domain = "dysmsapi.aliyuncs.com";//短信API产品域名（接口地址固定，无需修改）
        //替换成你的AK
        final String accessKeyId = prop.getAccessKeyId();//你的accessKeyId,参考本文档步骤2
        final String accessKeySecret = prop.getAccessKeySecret();//你的accessKeySecret，参考本文档步骤2
        //初始化ascClient,暂时不支持多region（请勿修改）
        IClientProfile profile = DefaultProfile.getProfile("cn-hangzhou", accessKeyId,
                accessKeySecret);
        DefaultProfile.addEndpoint("cn-hangzhou", "cn-hangzhou", product, domain);
        return new DefaultAcsClient(profile);
    }
}
