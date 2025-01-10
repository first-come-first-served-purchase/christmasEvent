package com.doosan.orderservice.controller;

import com.doosan.common.dto.ResponseDto;
import com.doosan.common.dto.order.CreateOrderReqDto;
import com.doosan.common.exception.BusinessRuntimeException;
import com.doosan.common.utils.JwtUtil;
import com.doosan.orderservice.dto.CreateOrderResDto;
import com.doosan.orderservice.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import java.util.List;

@RestController
@RequestMapping("/api/v1/reactive/orders")
@RequiredArgsConstructor
@Log4j2
public class ReactiveOrderController {
    private final OrderService orderService;
    private final JwtUtil jwtUtil;

    @PostMapping("/create-order")
    public Mono<ResponseEntity<ResponseDto<CreateOrderResDto>>> createOrder(
            @RequestHeader("Authorization") String token,
            @RequestBody List<CreateOrderReqDto> orderRequests) {
            
        return Mono.just(extractUserId(token))
            .flatMap(userId -> orderService.createReactiveOrder(userId, orderRequests))
            .map(result -> 
                ResponseEntity.ok(
                    ResponseDto.<CreateOrderResDto>builder()
                        .statusCode(HttpStatus.OK.value())
                        .resultMessage("주문 생성 성공")
                        .data(result)
                        .build()
                )
            )
            .onErrorResume(e -> 
                Mono.just(
                    ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(ResponseDto.<CreateOrderResDto>builder()
                            .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                            .resultMessage(e.getMessage())
                            .build()
                        )
                )
            );
    }

    private int extractUserId(String token) {
        if (token != null && token.startsWith(JwtUtil.BEARER_PREFIX)) {
            String actualToken = token.substring(7);
            if (jwtUtil.validateToken(actualToken)) {
                return jwtUtil.getUserIdFromToken(actualToken);
            }
            throw new BusinessRuntimeException("유효하지 않은 토큰입니다.");
        }
        throw new BusinessRuntimeException("토큰 형식이 올바르지 않습니다.");
    }
} 