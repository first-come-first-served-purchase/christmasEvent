package com.doosan.christmas.product.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter // 각 필드의 getter 메서드를 자동 생성
@RequiredArgsConstructor // 모든 final 필드에 대해 생성자 자동 생성
public enum ProductCategory {
    ELECTRONICS("전자제품"), // 전자제품
    CLOTHING("의류"),       // 의류
    FOOD("식품"),           // 식품
    BOOKS("도서");          // 도서

    private final String description; // 카테고리 설명

    /**
     * JSON 역직렬화 시 문자열 값을 Enum 타입으로 변환
     * @param value JSON에서 전달된 문자열
     * @return ProductCategory 매칭된 Enum 값
     * @throws IllegalArgumentException 유효하지 않은 값일 경우 예외 발생
     */
    @JsonCreator
    public static ProductCategory from(String value) {
        for (ProductCategory category : values()) {
            if (category.name().equalsIgnoreCase(value)) {
                return category; // 대소문자 구분 없이 Enum 매칭
            }
        }
        throw new IllegalArgumentException("Invalid category: " + value); // 유효하지 않은 값 처리
    }
}
