package com.doosan.christmas.product.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ProductCategory {
    ELECTRONICS("전자제품"),
    CLOTHING("의류"),
    FOOD("식품"),
    BOOKS("도서");

    private final String description;

    /**
     * JSON 역직렬화 시 문자열 값을 Enum 타입으로 변환
     */
    @JsonCreator
    public static ProductCategory from(String value) {
        for (ProductCategory category : values()) {
            if (category.name().equalsIgnoreCase(value)) {
                return category;
            }
        }
        throw new IllegalArgumentException("Invalid category: " + value);
    }
} 