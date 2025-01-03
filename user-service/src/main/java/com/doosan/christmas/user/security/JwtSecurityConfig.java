package com.doosan.christmas.user.security;


import com.doosan.christmas.user.config.JwtTokenProvider;
import com.doosan.christmas.user.service.RedisService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.config.annotation.SecurityConfigurerAdapter;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.DefaultSecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@RequiredArgsConstructor
public class JwtSecurityConfig extends SecurityConfigurerAdapter<DefaultSecurityFilterChain, HttpSecurity> {

    private final JwtTokenProvider jwtTokenProvider;
    private final RedisService redisService;

    @Override
    public void configure(HttpSecurity http) {
        JwtAuthenticationFilter customFilter = new JwtAuthenticationFilter(jwtTokenProvider, redisService);
        http.addFilterBefore(customFilter, UsernamePasswordAuthenticationFilter.class);
    }
} 