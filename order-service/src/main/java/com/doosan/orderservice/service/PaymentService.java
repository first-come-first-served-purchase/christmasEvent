package com.doosan.orderservice.service;

import com.doosan.orderservice.entity.PaymentStatus;
import com.doosan.orderservice.event.OrderItemEvent;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface PaymentService {
    void processPaymentSettlement(Long orderId, Long totalAmount, Long userId, List<OrderItemEvent> items);
    void recordPaymentFailure(Long orderId, Long userId, PaymentStatus status);
    void processRefund(Long orderId, Long amount);

    @Transactional
    void processPayment(Long orderId, Long totalAmount, Long userId);
}
