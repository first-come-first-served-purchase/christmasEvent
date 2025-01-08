package com.doosan.orderservice.controller;

import com.doosan.common.dto.ResponseDto;
import com.doosan.common.dto.ResponseMessage;
import com.doosan.common.dto.order.CreateOrderReqDto;
import com.doosan.common.exception.BusinessRuntimeException;
import com.doosan.common.utils.JwtUtil;
import com.doosan.common.utils.ParseRequestUtil;
import com.doosan.orderservice.dto.CreateOrderResDto;
import com.doosan.orderservice.dto.OrderStatusUpdateRequest;
import com.doosan.orderservice.dto.WishListDto;
import com.doosan.orderservice.dto.WishListOrderResponseDto;
import com.doosan.orderservice.service.OrderService;
import com.doosan.orderservice.service.WishListService;
import com.doosan.productservice.dto.ProductResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/orders")
public class OrderController {

    private final OrderService orderService;
    private final ParseRequestUtil parseRequestUtil;
    private final WishListService wishListService;

    @Autowired
    public OrderController(OrderService orderService, WishListService wishListService, JwtUtil jwtUtil) {
        this.orderService = orderService;
        this.wishListService = wishListService;
        this.parseRequestUtil = new ParseRequestUtil(jwtUtil);
    }

    // 주문 취소
    @PostMapping("/{orderId}/cancel")
    public ResponseEntity<ResponseDto<Void>> cancelOrder(
            HttpServletRequest request,
            @PathVariable int orderId) {
        int userId = parseRequestUtil.extractUserIdFromRequest(request);
        return orderService.cancelOrder(userId, orderId);
    }

    // 주문 반품
    @PostMapping("/{orderId}/return")
    public ResponseEntity<ResponseDto<Void>> requestReturn(
            HttpServletRequest request,
            @PathVariable int orderId) {
        int userId = parseRequestUtil.extractUserIdFromRequest(request);
        return orderService.requestReturn(userId, orderId);
    }

    // 주문 생성
    @PostMapping("create-order")
    public ResponseEntity<ResponseMessage> createOrder(HttpServletRequest request, @RequestBody List<CreateOrderReqDto> orderItems) {
        int userId = parseRequestUtil.extractUserIdFromRequest(request);

        CreateOrderResDto orderResponse = orderService.createOrder(userId, orderItems);

        ResponseMessage response = ResponseMessage.builder()
                .data(orderResponse)
                .statusCode(200)
                .resultMessage("Success")
                .build();

        return ResponseEntity.ok(response);
    }

    // 주문상태 수동 변경 로직
    @PutMapping("/status")
    public ResponseEntity<?> updateOrderStatus(@RequestBody OrderStatusUpdateRequest request) {
        return orderService.updateOrderStatus(request);
    }

    // WishList 조회
    @GetMapping("/wishlist")
    public ResponseEntity<ResponseDto<List<WishListDto>>> getWishList(HttpServletRequest request) {
        int userId = parseRequestUtil.extractUserIdFromRequest(request);
        return wishListService.getWishList(userId);
    }

    // WishList에 상품 추가
    @PostMapping("/wishlist/{productId}")
    public ResponseEntity<ResponseDto<Void>> addToWishList(
            HttpServletRequest request,
            @PathVariable Long productId,
            @RequestParam(defaultValue = "1") int quantity) {
        int userId = parseRequestUtil.extractUserIdFromRequest(request);
        return wishListService.addToWishList(userId, productId, quantity);
    }

    // WishList 상품 수량 수정
    @PutMapping("/wishlist/{productId}")
    public ResponseEntity<ResponseDto<Void>> updateWishListItem(
            HttpServletRequest request,
            @PathVariable Long productId,
            @RequestParam int quantity) {
        int userId = parseRequestUtil.extractUserIdFromRequest(request);
        return wishListService.updateWishListItem(userId, productId, quantity);
    }

    // WishList에서 상품 제거
    @DeleteMapping("/wishlist/{productId}")
    public ResponseEntity<ResponseDto<Void>> removeFromWishList(
            HttpServletRequest request,
            @PathVariable Long productId) {
        int userId = parseRequestUtil.extractUserIdFromRequest(request);
        return wishListService.removeFromWishList(userId, productId);
    }

    // WishList 상품들 주문
    @PostMapping("/wishlist/order")
    public ResponseEntity<ResponseDto<WishListOrderResponseDto>> orderFromWishList(
            HttpServletRequest request,
            @RequestBody List<Long> productIds) {
        int userId = parseRequestUtil.extractUserIdFromRequest(request);
        return wishListService.orderFromWishList(userId, productIds);
    }

    // Circuit Breaker 테스트용 API
    @GetMapping("/test/circuit-breaker/{productId}")
    public ResponseEntity<ResponseDto<ProductResponse>> testCircuitBreaker(@PathVariable Long productId) {
        try {
            ProductResponse response = orderService.getProductWithCircuitBreaker(productId);
            return ResponseEntity.ok(
                ResponseDto.<ProductResponse>builder()
                    .statusCode(HttpStatus.OK.value())
                    .resultMessage("성공")
                    .data(response)
                    .build()
            );
        } catch (BusinessRuntimeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ResponseDto.<ProductResponse>builder()
                    .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .resultMessage(e.getMessage())
                    .build()
                );
        }
    }

    @GetMapping("/test/random-error")
    public ResponseEntity<ResponseDto<ProductResponse>> testRandomError() {
        try {
            ProductResponse response = orderService.testRandomError();
            return ResponseEntity.ok(
                ResponseDto.<ProductResponse>builder()
                    .statusCode(HttpStatus.OK.value())
                    .resultMessage("성공")
                    .data(response)
                    .build()
            );
        } catch (BusinessRuntimeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ResponseDto.<ProductResponse>builder()
                    .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .resultMessage(e.getMessage())
                    .build()
                );
        }
    }

    @GetMapping("/test/timeout")
    public ResponseEntity<ResponseDto<ProductResponse>> testTimeout() {
        try {
            ProductResponse response = orderService.testTimeout();
            return ResponseEntity.ok(
                ResponseDto.<ProductResponse>builder()
                    .statusCode(HttpStatus.OK.value())
                    .resultMessage("성공")
                    .data(response)
                    .build()
            );
        } catch (BusinessRuntimeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ResponseDto.<ProductResponse>builder()
                    .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .resultMessage(e.getMessage())
                    .build()
                );
        }
    }

}
