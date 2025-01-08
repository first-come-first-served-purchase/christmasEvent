package com.doosan.orderservice.entity;

public enum PaymentStatus {
    PAYMENT_PENDING,    // 결제 진행 중
    PAYMENT_COMPLETED,  // 결제 완료
    PAYMENT_FAILED     // 결제 실패
}
