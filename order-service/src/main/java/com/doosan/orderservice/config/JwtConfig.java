package com.doosan.orderservice.config;

import com.doosan.common.utils.JwtUtil;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JwtConfig {
    
    @Bean
    public JwtUtil jwtUtil() {
        return new JwtUtil();
    }
} 