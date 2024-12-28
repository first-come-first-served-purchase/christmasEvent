package com.doosan.christmas.common.config;

import com.doosan.christmas.common.jwt.AccessDeniedHandlerException;
import com.doosan.christmas.common.jwt.AuthenticationEntryPointException;
import com.doosan.christmas.common.jwt.TokenProvider;
import com.doosan.christmas.member.service.UserDetailsServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.security.ConditionalOnDefaultWebSecurity;
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
@ConditionalOnDefaultWebSecurity
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
public class SecurityConfig {

    @Value("${jwt.secret}")
    String SECRET_KEY; // JWT 서명에 사용할 키
    private final TokenProvider tokenProvider; // JWT 관련 기능 제공
    private final UserDetailsServiceImpl userDetailsService; // 사용자 정보 로드 서비스
    private final AuthenticationEntryPointException authenticationEntryPointException; // 인증 실패 처리
    private final AccessDeniedHandlerException accessDeniedHandlerException; // 권한 실패 처리
    private final CorsConfig corsConfiguration; // CORS 설정

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(); // 비밀번호 암호화를 위한 Bean
    }

    @Bean
    @Order(SecurityProperties.BASIC_AUTH_ORDER)
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.cors();
        http.csrf().disable()
            .exceptionHandling()
            .authenticationEntryPoint(authenticationEntryPointException)
            .accessDeniedHandler(accessDeniedHandlerException)
            .and()
            .sessionManagement()
            .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            .and()
            .authorizeHttpRequests()
            // Swagger UI 접근을 가장 먼저 허용
            .requestMatchers(
                "/",
                "/swagger-ui/**",
                "/v3/api-docs/**",
                "/swagger-resources/**",
                "/swagger-ui.html",
                "/webjars/**",
                "/favicon.ico",
                "/error"
            ).permitAll()
            // 나머지 API 경로 설정
            .requestMatchers("/v1/users/**").permitAll()
            .requestMatchers("/v1/members/**").permitAll()
            .requestMatchers("/v1/products/**").permitAll()
            .requestMatchers("/v1/auth/**").permitAll()
            .requestMatchers("/v1/users/signup/admin").hasAuthority("ROLE_ADMIN")
            .requestMatchers("/v1/users/signup/seller").hasAuthority("ROLE_ADMIN")
            .anyRequest().authenticated();

        // JwtSecurityConfig 적용
        http.apply(new JwtSecurityConfig(SECRET_KEY, tokenProvider, userDetailsService));

        return http.build();
    }
}