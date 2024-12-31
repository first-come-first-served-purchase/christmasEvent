package com.doosan.christmas.order.config;

import feign.RequestInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Configuration
public class FeignClientConfig {

    @Value("${service.auth.token:eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzZXJ2aWNlIjoib3JkZXItc2VydmljZSJ9.dkXnz6QGz6QP5oNrqZzj9EF44BAHJz-n8DgZg_dTPZ4}")
    private String serviceToken;

    @Bean
    public RequestInterceptor requestInterceptor() {
        return requestTemplate -> {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                String token = attributes.getRequest().getHeader("Authorization");
                if (token != null) {
                    requestTemplate.header("Authorization", token);
                } else {
                    requestTemplate.header("Authorization", "Bearer " + serviceToken);
                }
            } else {
                requestTemplate.header("Authorization", "Bearer " + serviceToken);
            }
        };
    }
} 