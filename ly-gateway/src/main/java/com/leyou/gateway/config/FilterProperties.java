package com.leyou.gateway.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;
@Component
@Data
@ConfigurationProperties(prefix = "ly.filter")
public class FilterProperties {
private List<String> allowPaths;

}
