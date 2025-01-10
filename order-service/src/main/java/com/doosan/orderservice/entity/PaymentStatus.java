package com.doosan.orderservice.entity;

public enum PaymentStatus {
    PAYMENT_COMPLETED,  // 결제 완료
    PAYMENT_FAILED,    // 결제 실패
    CANCELLED,         // 결제 취소
    REFUNDED          // 환불 완료
}
