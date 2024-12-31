package com.doosan.christmas.gateway.config;//package com.doosan.christmas.common.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Getter;
import lombok.Setter;

@Configuration
@ConfigurationProperties(prefix = "jwt")
@Getter
@Setter
public class JwtConfig {
    private String secret;
    private long accessTokenValidityInSeconds;
    private long refreshTokenValidityInSeconds;
}