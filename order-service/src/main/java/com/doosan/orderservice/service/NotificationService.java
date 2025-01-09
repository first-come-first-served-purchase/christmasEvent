package com.doosan.orderservice.service;

public interface NotificationService {
    void sendOrderCompletionNotification(Long userId, Long orderId, Long amount); // 주문 완료 알림 전송
    void sendPaymentFailureNotification(Long userId, Long orderId); // 결제 실패 알림 전송
    void sendOrderCancellationNotification(Long userId, Long orderId); // 주문 취소 알림 전송
} 