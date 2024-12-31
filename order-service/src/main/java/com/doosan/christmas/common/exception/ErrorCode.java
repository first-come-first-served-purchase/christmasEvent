package com.doosan.christmas.common.exception;

import lombok.Getter;

@Getter
public enum ErrorCode {
    // 기존 코드...
    
    DUPLICATE_EMAIL("USER_001", "이미 가입된 이메일입니다."),
    EMAIL_SEND_FAILED("EMAIL_001", "이메일 전송에 실패했습니다.");
    
    private final String code;
    private final String message;
    
    ErrorCode(String code, String message) {
        this.code = code;
        this.message = message;
    }
} 