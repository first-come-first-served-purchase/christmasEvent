package com.doosan.christmas.user.exception;

import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(basePackages = "com.doosan.christmas.user")
public class UserServiceExceptionHandler extends com.doosan.christmas.common.exception.GlobalExceptionHandler {
    // 필요한 경우 user-service 전용 예외 처리 메서드 추가
} 