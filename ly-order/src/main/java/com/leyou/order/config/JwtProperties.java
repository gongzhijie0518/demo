package com.leyou.order.config;

import com.leyou.auth.utils.RsaUtils;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.security.PrivateKey;
import java.security.PublicKey;

@Slf4j
@Data
@Component
@ConfigurationProperties(prefix = "ly.jwt")
public class JwtProperties {
    private String pubKeyPath;//公钥路径
    private String cookName;//cookie的名字
    private PublicKey publicKey;//公钥

  @PostConstruct
  public void init() throws Exception {
      try {
          this.publicKey = RsaUtils.getPublicKey(pubKeyPath);
      }catch (Exception e){
          log.error("加载公钥失败",e.getMessage());
          throw new Exception(e);
      }
  }
}
