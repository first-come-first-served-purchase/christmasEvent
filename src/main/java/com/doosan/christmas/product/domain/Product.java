package com.doosan.christmas.product.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;


import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Slf4j
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // 상품 ID

    private String name; // 상품 이름

    private String description; // 상품 설명

    private BigDecimal price; // 상품 가격

    private Long stock; // 재고 수량

    private String imageUrl; // 상품 이미지 URL

    private Boolean isActive; // 상품 활성 상태

    @Enumerated(EnumType.STRING) // Enum 타입을 문자열로 저장
    private ProductCategory category; // 상품 카테고리

    @CreatedDate
    private LocalDateTime createdAt; // 생성시간

    @LastModifiedDate
    private LocalDateTime modifiedAt; // 수정시간

    public void increaseStock(Long quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than zero");
        }

        // 현재 재고 상태와 추가할 수량을 로깅
        log.info("상품 재고 증가 시작 - 기존 재고: {}, 추가할 수량: {}", this.stock, quantity);

        // 기존 재고에 수량을 더함
        this.stock += quantity;

        // 재고 증가 후 상태를 로깅
        log.info("상품 재고 증가 완료 - 증가된 후 재고: {}", this.stock);
    }
}
