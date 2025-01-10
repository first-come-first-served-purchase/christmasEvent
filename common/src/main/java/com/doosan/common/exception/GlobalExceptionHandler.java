package com.doosan.common.exception;

import com.doosan.common.dto.ResponseMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import reactor.core.publisher.Mono;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessRuntimeException.class)
    public Mono<ResponseEntity<ResponseMessage>> handleBusinessException(BusinessRuntimeException e) {
        log.error("비즈니스 예외 발생: {}", e.getMessage());
        return Mono.just(ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ResponseMessage.builder()
                        .statusCode(HttpStatus.BAD_REQUEST.value())
                        .resultMessage(e.getMessage())
                        .build()));
    }

    @ExceptionHandler(Exception.class)
    public Mono<ResponseEntity<ResponseMessage>> handleGeneralException(Exception e) {
        log.error("일반 예외 발생: ", e);
        return Mono.just(ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ResponseMessage.builder()
                        .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                        .resultMessage("서버 내부 오류가 발생했습니다.")
                        .build()));
    }
}
