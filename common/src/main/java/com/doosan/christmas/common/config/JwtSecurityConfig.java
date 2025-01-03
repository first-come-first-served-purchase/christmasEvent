//package com.doosan.christmas.common.config;
//
//
//
//import com.doosan.christmas.common.jwt.JwtAuthenticationWebFilter;
//
//import com.doosan.christmas.common.jwt.JwtTokenProvider;
//
//import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
//
//import org.springframework.context.annotation.Bean;
//
//import org.springframework.context.annotation.Configuration;
//
//import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
//
//import org.springframework.security.config.web.server.ServerHttpSecurity;
//
//import org.springframework.security.web.server.SecurityWebFilterChain;
//
//
//
//@Configuration
//
//@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.REACTIVE)
//
//public class JwtSecurityConfig {
//
//
//
//    private final JwtTokenProvider jwtTokenProvider;
//
//
//
//    public JwtSecurityConfig(JwtTokenProvider jwtTokenProvider) {
//
//        this.jwtTokenProvider = jwtTokenProvider;
//
//    }
//
//
//
//    @Bean(name = "jwtSecurityFilterChain")
//
//    public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {
//
//        http
//
//                .csrf().disable()
//
//                .addFilterAt(new JwtAuthenticationWebFilter(jwtTokenProvider), SecurityWebFiltersOrder.AUTHENTICATION);
//
//
//
//        return http.build();
//
//    }
//
//}