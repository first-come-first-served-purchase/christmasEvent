package com.doosan.orderservice.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class OrderStatusUpdateRequest {
    private Integer orderId; // 주문아이디
    private String status; // 주문상태
} 