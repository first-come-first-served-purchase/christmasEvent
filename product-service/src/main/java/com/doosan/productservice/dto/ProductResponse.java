package com.doosan.productservice.dto;

import com.doosan.common.enums.ProductCategory;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class ProductResponse {
    private Long id; // 상품 아이디
    private String name; // 상품 이름
    private Long price; // 상품 가격
    private String description; // 상품 설명
    private ProductCategory category; // 상품 카테고리
    private String imageUrl; // 상품 이미지 url
    private Integer quantity; // 수량
}
