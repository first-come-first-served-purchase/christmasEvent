package com.doosan.christmas.user.exception;

import com.doosan.christmas.common.dto.ResponseDto;
import com.doosan.christmas.common.exception.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class SecurityExceptionHandler {

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ResponseDto<Void>> handleAuthenticationException(AuthenticationException e) {
        log.error("Authentication error occurred: {}", e.getMessage());
        return ResponseEntity
                .status(ErrorCode.INVALID_CREDENTIALS.getStatus())
                .body(ResponseDto.error(ErrorCode.INVALID_CREDENTIALS));
    }
} 