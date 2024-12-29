package com.doosan.christmas.user.exception;

import com.doosan.christmas.common.exception.CustomException;
import com.doosan.christmas.common.exception.ErrorCode;
import com.doosan.christmas.common.dto.ResponseDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.dao.DataIntegrityViolationException;
import jakarta.validation.ConstraintViolationException;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(CustomException.class)
    public ResponseEntity<ResponseDto<Void>> handleCustomException(CustomException e) {
        log.error("Custom error occurred: {}", e.getMessage());
        return ResponseEntity
                .status(e.getErrorCode().getStatus())
                .body(ResponseDto.error(e.getErrorCode()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ResponseDto<Void>> handleValidationExceptions(MethodArgumentNotValidException e) {
        log.error("Validation error occurred: {}", e.getBindingResult().getFieldError().getDefaultMessage());
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ResponseDto.error(ErrorCode.INVALID_INPUT_VALUE));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ResponseDto<Void>> handleAllExceptions(Exception e) {
        log.error("Unexpected error occurred", e);
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ResponseDto.error(ErrorCode.INTERNAL_SERVER_ERROR));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseDto<Void> handleDataIntegrityViolationException(DataIntegrityViolationException e) {
        log.error("데이터 무결성 위반 오류 발생: {}", e.getMessage());
        
        String message = "데이터 처리 중 오류가 발생했습니다.";
        if (e.getCause() instanceof ConstraintViolationException) {
            message = "필수 입력값이 누락되었습니다.";
        }
        
        return ResponseDto.fail(message);
    }
} 