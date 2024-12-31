package com.doosan.christmas.user.config;//package com.doosan.christmas.common.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "jwt")
@Getter
@Setter
public class JwtConfig {
    private String secret;
    private long accessTokenValidityInSeconds;
    private long refreshTokenValidityInSeconds;
}