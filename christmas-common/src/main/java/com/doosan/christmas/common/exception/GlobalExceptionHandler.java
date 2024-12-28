package com.doosan.christmas.common.exception;

import com.doosan.christmas.common.dto.ResponseDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(CustomException.class)
    protected ResponseEntity<ResponseDto<?>> handleCustomException(CustomException e) {
        log.error("CustomException: {}", e.getMessage());
        return ResponseEntity
                .status(e.getErrorCode().getStatus())
                .body(ResponseDto.fail(e.getErrorCode().name(), e.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    protected ResponseEntity<ResponseDto<?>> handleValidationException(MethodArgumentNotValidException e) {
        log.error("ValidationException: {}", e.getMessage());
        String errorMessage = e.getBindingResult()
                .getAllErrors()
                .get(0)
                .getDefaultMessage();
        return ResponseEntity
                .badRequest()
                .body(ResponseDto.fail("VALIDATION_ERROR", errorMessage));
    }

    @ExceptionHandler(BadCredentialsException.class)
    protected ResponseEntity<ResponseDto<?>> handleBadCredentialsException(BadCredentialsException e) {
        log.error("BadCredentialsException: {}", e.getMessage());
        return ResponseEntity
                .status(ErrorCode.INVALID_PASSWORD.getStatus())
                .body(ResponseDto.fail("INVALID_CREDENTIALS", "이메일 또는 비밀번호가 일치하지 않습니다."));
    }

    @ExceptionHandler(AccessDeniedException.class)
    protected ResponseEntity<ResponseDto<?>> handleAccessDeniedException(AccessDeniedException e) {
        log.error("AccessDeniedException: {}", e.getMessage());
        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(ResponseDto.fail("ACCESS_DENIED", "접근 권한이 없습니다."));
    }

    @ExceptionHandler(Exception.class)
    protected ResponseEntity<ResponseDto<?>> handleException(Exception e) {
        log.error("Exception: ", e);
        return ResponseEntity
                .status(ErrorCode.INTERNAL_SERVER_ERROR.getStatus())
                .body(ResponseDto.fail("INTERNAL_SERVER_ERROR", "서버 내부 오류가 발생했습니다."));
    }
} 