package com.doosan.christmas.product.dto.responseDto;

import com.doosan.christmas.product.domain.Product;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.math.BigDecimal;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductResponseDto {
    private Long id; // 상품 ID
    private String name; // 상품 이름
    private String description; // 상품 설명
    private BigDecimal price; // 상품 가격
    private String imageUrl; // 상품 이미지 URL

    /**
     * Product 엔티티를 ProductResponseDto로 변환
     */
    public static ProductResponseDto from(Product product) {
        return ProductResponseDto.builder()
                .id(product.getId()) // ID 매핑
                .name(product.getName()) // 이름 매핑
                .description(product.getDescription()) // 설명 매핑
                .price(product.getPrice()) // 가격 매핑
                .imageUrl(product.getImageUrl()) // 이미지 URL 매핑
                .build();
    }
}