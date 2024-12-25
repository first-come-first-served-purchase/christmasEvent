package com.doosan.christmas.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.ApiKey;
import springfox.documentation.service.AuthorizationScope;
import springfox.documentation.service.SecurityReference;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spi.service.contexts.SecurityContext;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;
import java.util.Arrays;
import java.util.List;

import lombok.extern.slf4j.Slf4j;

/**
 * Swagger 설정 클래스
 * - API 문서를 자동으로 생성하기 위한 설정
 */
@Slf4j
@Configuration
@EnableSwagger2
public class SwaggerConfig {
    /**
     * Swagger Docket Bean 생성
     */
    @Bean
    public Docket api() {
        log.info("Swagger Docket 설정 시작");
        Docket docket = new Docket(DocumentationType.OAS_30) // Swagger 3.0 문서 타입 설정
                .useDefaultResponseMessages(true) // 기본 응답 메시지 사용 여부
                .securityContexts(Arrays.asList(securityContext())) // 인증/권한 보안 설정 추가
                .securitySchemes(Arrays.asList(apiKey())) // API Key 인증 방식 설정
                .select()
                .apis(RequestHandlerSelectors.basePackage("com.example.demo")) // API 문서화 대상 패키지 설정
                .paths(PathSelectors.any()) // 모든 URL 경로 대상
                .build()
                .apiInfo(apiInfo()); // API 정보 설정
        log.info("Swagger Docket 설정 완료");
        return docket;
    }

    /**
     * API 정보 설정
     */
    private ApiInfo apiInfo() {
        log.info("Swagger API 정보 설정 시작");
        ApiInfo apiInfo = new ApiInfoBuilder()
                .title("스프링 부트 API") // API 제목
                .description("스프링 부트 API Swagger 페이지") // API 설명
                .version("1.0") // API 버전
                .build();
        log.debug("API 정보 설정 완료: {}", apiInfo);
        return apiInfo;
    }

    /**
     * 보안 컨텍스트 설정
     * - API 문서에서 보안 설정을 처리하기 위한 컨텍스트
     */
    private SecurityContext securityContext() {
        log.info("Swagger 보안 컨텍스트(SecurityContext) 설정 시작");
        SecurityContext securityContext = SecurityContext.builder()
                .securityReferences(defaultAuth()) // 기본 인증 정보 설정
                .build();
        log.debug("보안 컨텍스트(SecurityContext) 설정 완료: {}", securityContext);
        return securityContext;
    }

    /**
     * 기본 인증 정보 설정
     * - 인증 스코프 및 헤더 기반 인증 처리 정의
     */
    private List<SecurityReference> defaultAuth() {
        log.info("Swagger 기본 인증(SecurityReference) 설정 시작");
        AuthorizationScope authorizationScope = new AuthorizationScope("global", "accessEverything"); // 인증 범위 설정
        AuthorizationScope[] authorizationScopes = new AuthorizationScope[1];
        authorizationScopes[0] = authorizationScope;
        List<SecurityReference> securityReferences = Arrays.asList(new SecurityReference("Authorization", authorizationScopes));
        log.debug("기본 인증(SecurityReference) 설정 완료: {}", securityReferences);

        return securityReferences;
    }

    /**
     * API Key 인증 방식 설정
     * - 헤더 기반 인증 방식 정의
     */
    private ApiKey apiKey() {
        log.info("Swagger API Key 설정 시작");

        ApiKey apiKey = new ApiKey("Authorization", "Authorization", "header"); // API Key 정보 정의
        log.debug("API Key 설정 완료: {}", apiKey);

        return apiKey;
    }
}