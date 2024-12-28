package com.doosan.christmas.api.controller;

import com.doosan.christmas.common.dto.OrderRequest;
import com.doosan.christmas.common.dto.OrderResponse;
import com.doosan.christmas.common.service.ChristmasEventService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {
    private final ChristmasEventService eventService;
    
    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(@RequestBody OrderRequest request) {
        // 주문 처리 및 할인 계산
        return ResponseEntity.ok(OrderResponse.empty());
    }
    
    @GetMapping("/{orderId}")
    public ResponseEntity<OrderResponse> getOrder(@PathVariable Long orderId) {
        // 주문 조회
        return ResponseEntity.ok(OrderResponse.empty());
    }
} 