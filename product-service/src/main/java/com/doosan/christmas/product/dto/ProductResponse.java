package com.doosan.christmas.product.dto;

import com.doosan.christmas.product.domain.Product;
import com.doosan.christmas.product.domain.ProductCategory;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import java.math.BigDecimal;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductResponse {
    private Long id;
    private String name;
    private BigDecimal price;
    private Long stock;
    private String description;
    private ProductCategory category;
    private String imageUrl;
    
    public static ProductResponse from(Product product) {
        return ProductResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .price(product.getPrice())
                .stock(product.getStock())
                .description(product.getDescription())
                .category(product.getCategory())
                .imageUrl(product.getImageUrl())
                .build();
    }
} 