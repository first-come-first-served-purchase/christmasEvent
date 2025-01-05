package com.doosan.orderservice.controller;


import com.doosan.common.dto.ResponseDto;
import com.doosan.common.dto.ResponseMessage;
import com.doosan.common.dto.order.CreateOrderReqDto;
import com.doosan.common.utils.ParseRequestUtil;
import com.doosan.orderservice.dto.CreateOrderResDto;
import com.doosan.orderservice.dto.OrderStatusUpdateRequest;
import com.doosan.orderservice.service.OrderService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/order")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    // 주문 취소
    @PostMapping("/{orderId}/cancel")
    public ResponseEntity<ResponseDto<Void>> cancelOrder(
            HttpServletRequest request,
            @PathVariable int orderId) {
        int userId = new ParseRequestUtil().extractUserIdFromRequest(request);
        return orderService.cancelOrder(userId, orderId);
    }

    // 주문 반품
    @PostMapping("/{orderId}/return")
    public ResponseEntity<ResponseDto<Void>> requestReturn(
            HttpServletRequest request,
            @PathVariable int orderId) {
        int userId = new ParseRequestUtil().extractUserIdFromRequest(request);
        return orderService.requestReturn(userId, orderId);
    }

    // 주문 생성
    @PostMapping("create-order")
    public ResponseEntity<ResponseMessage> createOrder(HttpServletRequest request, @RequestBody List<CreateOrderReqDto> orderItems) {
        int userId = new ParseRequestUtil().extractUserIdFromRequest(request);

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

}
