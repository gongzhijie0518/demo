package com.leyou.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

@Configuration
public class GlobalCorsConfig {
    @Bean
    public CorsFilter corsFilter(CorsProperties prop) {
        //1.添加CORS配置信息
        CorsConfiguration config = new CorsConfiguration();
        //1) 允许的域,不要写*，否则cookie就无法使用了
        prop.getAllowedOrigins().forEach(config::addAllowedOrigin);
//        config.addAllowedOrigin("http://manage.leyou.com");
        //2) 是否发送Cookie信息
//        config.setAllowCredentials(true);
        config.setAllowCredentials(prop.getAllowCredentials());
        //3) 允许的请求方式
//        config.addAllowedMethod("OPTIONS");
//        config.addAllowedMethod("HEAD");
//        config.addAllowedMethod("GET");
//        config.addAllowedMethod("PUT");
//        config.addAllowedMethod("POST");
//        config.addAllowedMethod("DELETE");
//        config.addAllowedMethod("PATCH");
        prop.getAllowedMethods().forEach(config::addAllowedMethod);
        // 4）允许的头信息
//        config.addAllowedHeader("*");
       prop.getAllowedHeaders().forEach(config::addAllowedHeader);
        //5)跨域许可的有效时间
//        config.setMaxAge(1800L);
        config.setMaxAge(prop.getMaxAge());
        //2.添加映射路径，我们拦截一切请求
        UrlBasedCorsConfigurationSource configSource = new UrlBasedCorsConfigurationSource();
//        configSource.registerCorsConfiguration("/**", config);
        configSource.registerCorsConfiguration(prop.getFilterPath(), config);
        //3.返回新的CorsFilter.
        return new CorsFilter(configSource);
    }
}