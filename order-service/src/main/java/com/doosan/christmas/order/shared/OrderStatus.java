package com.doosan.christmas.order.shared;

public enum OrderStatus {
    ORDER_RECEIVED,    // 주문 접수
    DELIVERING,       // 배송 중
    DELIVERED,        // 배송됨
    DELIVERY_COMPLETED, // 배송 완료
    CANCELLED,        // 취소됨
    RETURN_REQUESTED,  // 반품 요청
    RETURN_COMPLETED   // 반품 완료
}