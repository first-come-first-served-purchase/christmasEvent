package com.doosan.christmas.common.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JacksonConfig {

    // 날짜,시간 형식을 처리 하기 위한 빈 설정
    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper objectMapper = new ObjectMapper(); // ObjectMapper 생성
        objectMapper.registerModule(new JavaTimeModule()); // 날짜,시간 타입을 처리 하는 모듈 등록
        return objectMapper; // 설정된 ObjectMapper 반환
    }
}

