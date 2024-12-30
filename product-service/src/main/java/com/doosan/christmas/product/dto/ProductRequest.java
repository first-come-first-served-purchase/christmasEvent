package com.doosan.christmas.product.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductRequest {
    
    @NotBlank(message = "상품명은 필수입니다.")
    private String name;
    
    @NotNull(message = "가격은 필수입니다.")
    @Min(value = 0, message = "가격은 0원 이상이어야 합니다.")
    private Integer price;
    
    @NotNull(message = "재고는 필수입니다.")
    @Min(value = 0, message = "재고는 0개 이상이어야 합니다.")
    private Integer stock;
    
    private String description;
    
    @NotBlank(message = "카테고리는 필수입니다.")
    private String category;
    
    private String imageUrl;
} 