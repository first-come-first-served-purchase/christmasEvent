package com.doosan.orderservice.controller;

import com.doosan.common.dto.ResponseDto;
import com.doosan.common.exception.BusinessRuntimeException;
import com.doosan.common.utils.JwtUtil;
import com.doosan.common.utils.ParseRequestUtil;
import com.doosan.orderservice.dto.OrderInfoResponse;
import com.doosan.orderservice.dto.OrderStatusUpdateRequest;
import com.doosan.orderservice.dto.WishListDto;
import com.doosan.orderservice.dto.WishListOrderResponseDto;
import com.doosan.orderservice.service.OrderService;
import com.doosan.orderservice.service.WishListService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
@Log4j2
public class OrderController {

    private final OrderService orderService;
    private final WishListService wishListService;
    private final ParseRequestUtil parseRequestUtil;
    private final JwtUtil jwtUtil;

    // 토큰에서 id 추출
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

    // 주문 취소
    @PostMapping("/{orderId}/cancel")
    public Mono<ResponseEntity<ResponseDto<Void>>> cancelOrder(
            @PathVariable int orderId,
            @RequestHeader("Authorization") String token) {
        return orderService.cancelOrder(extractUserId(token), orderId);
    }

    // 주문 반품
    @PostMapping("/{orderId}/return")
    public Mono<ResponseEntity<ResponseDto<Void>>> requestReturn(
            @PathVariable int orderId,
            @RequestHeader("Authorization") String token) {
        return orderService.requestReturn(extractUserId(token), orderId);
    }

    // 주문 상태 업데이트
    @PutMapping("/status")
    public ResponseEntity<?> updateOrderStatus(@RequestBody OrderStatusUpdateRequest request) {
        return orderService.updateOrderStatus(request);
    }

    // 위시리스트 조회
    @GetMapping("/wishlist")
    public Mono<ResponseEntity<ResponseDto<List<WishListDto>>>> getWishList(ServerHttpRequest request) {
        return parseRequestUtil.extractUserIdFromRequest(request)
                .flatMap(userId -> wishListService.getWishList(userId));
    }

    // 위시리스트에 상품 담기
    @PostMapping("/wishlist/{productId}")
    public Mono<ResponseEntity<ResponseDto<Void>>> addToWishList(
            ServerHttpRequest request,
            @PathVariable Long productId,
            @RequestParam(defaultValue = "1") int quantity) {
        return parseRequestUtil.extractUserIdFromRequest(request)
                .flatMap(userId -> wishListService.addToWishList(userId, productId, quantity));
    }

    // 위시리스트에 담긴 상품 수량 변경
    @PutMapping("/wishlist/{productId}")
    public Mono<ResponseEntity<ResponseDto<Void>>> updateWishListItem(
            ServerHttpRequest request,
            @PathVariable Long productId,
            @RequestParam(defaultValue = "1") int quantity) {
        return parseRequestUtil.extractUserIdFromRequest(request)
                .flatMap(userId -> wishListService.updateWishListItem(userId, productId, quantity));
    }

    // 위시리스트에서 상품 제거
    @DeleteMapping("/wishlist/{productId}")
    public Mono<ResponseEntity<ResponseDto<Void>>> removeFromWishList(
            ServerHttpRequest request,
            @PathVariable Long productId) {
        return parseRequestUtil.extractUserIdFromRequest(request)
                .flatMap(userId -> wishListService.removeFromWishList(userId, productId));
    }

    // 위시리스트에서 상품 주문
    @PostMapping("/wishlist/order")
    public ResponseEntity<ResponseDto<WishListOrderResponseDto>> orderFromWishList(
            @RequestHeader("Authorization") String token,
            @RequestBody List<Long> productIds) {
        int userId = extractUserId(token);
        return wishListService.orderFromWishList(userId, productIds);
    }


    @GetMapping("/{orderId}")
    public Mono<ResponseEntity<ResponseDto<OrderInfoResponse>>> getOrderInfo(@PathVariable Long orderId) {
        return Mono.fromCallable(() -> orderService.getOrderInfo(orderId))
                .map(orderInfo -> ResponseEntity.ok(
                        ResponseDto.<OrderInfoResponse>builder()
                                .statusCode(HttpStatus.OK.value())
                                .resultMessage("주문 조회 성공")
                                .data(orderInfo)
                                .build()
                ))
                .onErrorResume(e -> Mono.just(
                        ResponseEntity.badRequest()
                                .body(ResponseDto.<OrderInfoResponse>builder()
                                        .statusCode(HttpStatus.BAD_REQUEST.value())
                                        .resultMessage(e.getMessage())
                                        .build())
                ));
    }

    @GetMapping("/user")
    public Mono<ResponseEntity<ResponseDto<Page<OrderInfoResponse>>>> getUserOrders(
            ServerHttpRequest request,
            @RequestParam(defaultValue = "0") Long page,
            @RequestParam(defaultValue = "10") Long size,
            @RequestParam(required = false) String sort,
            @RequestParam(defaultValue = "desc") String direction,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date startDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date endDate) {

        return parseRequestUtil.extractUserIdFromRequest(request)
                .flatMap(userId ->
                        Mono.fromCallable(() -> orderService.getUserOrders(
                                Long.valueOf(userId),
                                page,
                                size,
                                sort,
                                direction,
                                status,
                                startDate,
                                endDate))
                )
                .map(orders -> ResponseEntity.ok(
                        ResponseDto.<Page<OrderInfoResponse>>builder()
                                .statusCode(HttpStatus.OK.value())
                                .resultMessage("주문 목록 조회 성공")
                                .data(orders)
                                .build()
                ));
    }


}
