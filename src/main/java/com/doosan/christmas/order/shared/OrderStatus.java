package com.doosan.christmas.order.shared;

import lombok.Getter;

@Getter
public enum OrderStatus {
    ORDER_RECEIVED("주문 접수"),
    DELIVERING("배송 중"),
    DELIVERY_COMPLETED("배송 완료"),
    DELIVERED("배송 완료"),
    CANCELLED("취소 완료"),
    RETURN_REQUESTED("반품 신청"),
    RETURN_COMPLETED("반품 완료");

    private final String description;

    OrderStatus(String description) {
        this.description = description;
    }

}