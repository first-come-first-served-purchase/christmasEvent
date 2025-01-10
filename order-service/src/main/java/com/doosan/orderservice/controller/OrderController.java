package com.doosan.orderservice.controller;

import com.doosan.common.dto.ResponseDto;
import com.doosan.common.exception.BusinessRuntimeException;
import com.doosan.common.utils.JwtUtil;
import com.doosan.common.utils.ParseRequestUtil;
import com.doosan.orderservice.dto.OrderStatusUpdateRequest;
import com.doosan.orderservice.dto.WishListDto;
import com.doosan.orderservice.dto.WishListOrderResponseDto;
import com.doosan.orderservice.service.OrderService;
import com.doosan.orderservice.service.WishListService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

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

}
