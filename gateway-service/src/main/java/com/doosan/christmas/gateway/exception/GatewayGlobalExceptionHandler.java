package com.doosan.christmas.gateway.exception;

import com.doosan.christmas.common.dto.ResponseDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Slf4j
@Component("gatewayGlobalExceptionHandler")
@Order(-2)
public class GatewayGlobalExceptionHandler implements ErrorWebExceptionHandler {
    
    private final ObjectMapper objectMapper;

    public GatewayGlobalExceptionHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
        ServerHttpResponse response = exchange.getResponse();
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
        
        ResponseDto<Object> errorResponse;
        if (ex instanceof AuthenticationException) {
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            errorResponse = ResponseDto.fail("AUTH_ERROR", "인증에 실패했습니다.");
        } else {
            response.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR);
            errorResponse = ResponseDto.fail("AUTH_ERROR",ex.getMessage());
            log.error("Server Error:", ex);
        }

        try {
            byte[] bytes = objectMapper.writeValueAsBytes(errorResponse);
            DataBuffer buffer = response.bufferFactory().wrap(bytes);
            return response.writeWith(Mono.just(buffer));
        } catch (Exception e) {
            log.error("Error writing response", e);
            return Mono.error(e);
        }
    }
} 