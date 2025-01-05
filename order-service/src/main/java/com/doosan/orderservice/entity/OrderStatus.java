package com.doosan.orderservice.entity;

public enum OrderStatus {
    ORDER_COMPLETE("주문완료"),
    DELIVERING("배송중"),
    DELIVERY_COMPLETE("배송완료"),
    CANCEL_COMPLETE("취소완료"),
    RETURN_REQUEST("반품신청"),
    RETURN_COMPLETE("반품완료");

    private final String description;

    OrderStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
