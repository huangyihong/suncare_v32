package com.ai.modules.config.controller;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Map;

@Data
@Component
@ConfigurationProperties(prefix = "gbdpweb")
public class GbdpWebUrlProperties {
    private Map<String,String> url;
}
