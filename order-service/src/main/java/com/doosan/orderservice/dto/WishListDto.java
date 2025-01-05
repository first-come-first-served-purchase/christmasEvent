package com.doosan.orderservice.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class WishListDto {
    private Long productId; // 상품 아이디
    private Integer quantity; // 수량
    private String productName; // 상품이름
    private Long price; // 가격
    private String description; // 상품설명
} 