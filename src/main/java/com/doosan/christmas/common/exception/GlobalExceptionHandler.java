package com.doosan.christmas.common.exception;

import com.doosan.christmas.product.dto.responseDto.ResponseDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
    
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseDto<?> handleIllegalArgumentException(IllegalArgumentException e) {
        log.error("IllegalArgumentException", e);
        return ResponseDto.fail("INVALID_REQUEST", e.getMessage());
    }
    
    @ExceptionHandler(Exception.class)
    public ResponseDto<?> handleException(Exception e) {
        log.error("Unexpected error", e);
        return ResponseDto.fail("SERVER_ERROR", "서버 오류가 발생했습니다.");
    }
} 