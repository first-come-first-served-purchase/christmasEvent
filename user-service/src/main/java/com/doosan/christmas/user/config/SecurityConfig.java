package com.doosan.christmas.user.config;



import com.doosan.christmas.user.security.JwtSecurityConfig;
import com.doosan.christmas.user.service.RedisService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtTokenProvider jwtTokenProvider;
    private final RedisService redisService;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf().disable()
            .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            .and()
            .authorizeHttpRequests()
            .requestMatchers(
                "/api/auth/signup",
                "/api/v1/users/**",
                "/api/v1/auth/**",
                "/api/v1/auth/user/**",
                "/api/v1/auth/login",
                "/api/auth/login",
                "/api/auth/email",
                "/api/auth/verify-code",
                "/api/auth/refresh",
                "/actuator/**",
                "/v3/api-docs/**",
                "/swagger-ui/**",
                "/swagger-resources/**"
            ).permitAll()
            .requestMatchers("/api/auth/logout").authenticated()
            .anyRequest().authenticated()
            .and()
            .apply(new JwtSecurityConfig(jwtTokenProvider, redisService));

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}