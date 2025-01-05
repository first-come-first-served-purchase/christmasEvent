package com.doosan.orderservice.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class OrderItemDto {
    private Long productId; // 상품아이디
    private String productName; //상품이름
    private int quantity;//수량
    private long price;//가격
} 