package com.doosan.productservice.dto;

import com.doosan.common.enums.ProductCategory;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductDto {

    private Long id; // 상품 ID

    private String name; // 상품명

    private Long price; // 상품 가격

    private Long quantity; // 상품 수량

    private ProductCategory category; // 상품 카테고리

    private String description; // 상품 설명
}