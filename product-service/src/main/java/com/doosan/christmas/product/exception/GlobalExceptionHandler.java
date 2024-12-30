package com.doosan.christmas.product.exception;

import com.doosan.christmas.common.dto.ResponseDto;
import com.doosan.christmas.common.exception.CustomException;
import com.doosan.christmas.common.exception.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(CustomException.class)
    public ResponseDto<Void> handleCustomException(CustomException e) {
        log.error("CustomException: {}", e.getMessage());
        return ResponseDto.fail(e.getErrorCode(), e.getErrorCode().getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseDto<Void> handleValidationException(MethodArgumentNotValidException e) {
        log.error("Validation error: {}", e.getMessage());
        String errorMessage = e.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .findFirst()
                .orElse("유효성 검사 실패");
        return ResponseDto.fail(ErrorCode.INVALID_INPUT_VALUE, errorMessage);
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResponseDto<Void> handleException(Exception e) {
        log.error("Unexpected error occurred", e);
        return ResponseDto.fail(ErrorCode.INTERNAL_SERVER_ERROR, "서버 내부 오류가 발생했습니다.");
    }
} 