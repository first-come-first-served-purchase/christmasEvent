package com.doosan.productservice.dto;

import com.doosan.common.enums.ProductCategory;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProductResponse {
    private Long id;
    private String name;
    private Long price;
    private String description;
    private ProductCategory category;
    private String imageUrl;
    private Integer quantity;
}
