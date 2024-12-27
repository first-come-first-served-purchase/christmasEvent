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
        http.cors(); // CORS 설정 활성화
        http.csrf().disable() // CSRF 보호 비활성화
                .exceptionHandling()
                .authenticationEntryPoint(authenticationEntryPointException) // 인증 실패 시 처리
                .accessDeniedHandler(accessDeniedHandlerException) // 권한 실패 시 처리
                .and()
                .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS) // 세션을 사용하지 않는 Stateless 방식 설정
                .and()
                .authorizeRequests()
                .antMatchers("/v1/users/**").permitAll() // 특정 경로 모두 허용
                .antMatchers("/v1/members/**").permitAll() // 특정 경로 모두 허용
                .antMatchers("/v1/products/**").permitAll() // 특정 경로 모두 허용
                .antMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
                .antMatchers("/v1/users/signup/admin").hasAuthority("ROLE_ADMIN") // 관리자만 접근
                .antMatchers("/v1/users/signup/seller").hasAuthority("ROLE_ADMIN") // 관리자만 접근
                .antMatchers("/v1/auth/**").permitAll()
                .antMatchers( // Swagger 관련 경로 허용
                        "/v2/api-docs",
                        "/swagger-resources",
                        "/swagger-resources/**",
                        "/configuration/ui",
                        "/configuration/security",
                        "/swagger-ui.html",
                        "/webjars/**",
                        "/v3/api-docs/**",
                        "/swagger-ui/**").permitAll()
                .anyRequest().authenticated()
                .and()
                .addFilter(corsConfiguration.corsFilter()) // CORS 필터 추가
                .apply(new JwtSecurityConfig(SECRET_KEY, tokenProvider, userDetailsService)); // JWT 관련 설정 적용
        return http.build();
    }
}
