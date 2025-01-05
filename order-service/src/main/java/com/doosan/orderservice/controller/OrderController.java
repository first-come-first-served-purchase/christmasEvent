package com.doosan.orderservice.controller;

import com.doosan.common.dto.ResponseDto;
import com.doosan.common.dto.ResponseMessage;
import com.doosan.common.dto.order.CreateOrderReqDto;
import com.doosan.common.utils.JwtUtil;
import com.doosan.common.utils.ParseRequestUtil;
import com.doosan.orderservice.dto.CreateOrderResDto;
import com.doosan.orderservice.dto.OrderStatusUpdateRequest;
import com.doosan.orderservice.dto.WishListDto;
import com.doosan.orderservice.dto.WishListOrderResponseDto;
import com.doosan.orderservice.service.OrderService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/order")
public class OrderController {

    private final OrderService orderService;
    private final ParseRequestUtil parseRequestUtil;

    @Autowired
    public OrderController(OrderService orderService, JwtUtil jwtUtil) {
        this.orderService = orderService;
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
        return orderService.getWishList(userId);
    }

    // WishList에 상품 추가
    @PostMapping("/wishlist/{productId}")
    public ResponseEntity<ResponseDto<Void>> addToWishList(
            HttpServletRequest request,
            @PathVariable Long productId,
            @RequestParam(defaultValue = "1") int quantity) {
        int userId = parseRequestUtil.extractUserIdFromRequest(request);
        return orderService.addToWishList(userId, productId, quantity);
    }

    // WishList 상품 수량 수정
    @PutMapping("/wishlist/{productId}")
    public ResponseEntity<ResponseDto<Void>> updateWishListItem(
            HttpServletRequest request,
            @PathVariable Long productId,
            @RequestParam int quantity) {
        int userId = parseRequestUtil.extractUserIdFromRequest(request);
        return orderService.updateWishListItem(userId, productId, quantity);
    }

    // WishList에서 상품 제거
    @DeleteMapping("/wishlist/{productId}")
    public ResponseEntity<ResponseDto<Void>> removeFromWishList(
            HttpServletRequest request,
            @PathVariable Long productId) {
        int userId = parseRequestUtil.extractUserIdFromRequest(request);
        return orderService.removeFromWishList(userId, productId);
    }

    // WishList 상품들 주문
    @PostMapping("/wishlist/order")
    public ResponseEntity<ResponseDto<WishListOrderResponseDto>> orderFromWishList(
            HttpServletRequest request,
            @RequestBody List<Long> productIds) {
        int userId = parseRequestUtil.extractUserIdFromRequest(request);
        return orderService.orderFromWishList(userId, productIds);
    }

}
