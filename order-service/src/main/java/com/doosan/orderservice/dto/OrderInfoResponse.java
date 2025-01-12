package com.doosan.orderservice.dto;

import com.doosan.orderservice.entity.OrderStatus;
import com.doosan.orderservice.entity.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderInfoResponse {
    private Long orderId;
    private Long userId;
    private Date orderDate;
    private Long totalPrice;
    private OrderStatus status;
    private PaymentStatus paymentStatus;
    private List<OrderItemResponse> orderItems;
}