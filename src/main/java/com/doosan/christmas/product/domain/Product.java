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
@Table(name = "product")
@NoArgsConstructor
@AllArgsConstructor
@Slf4j
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // 상품 ID

    @Column(name = "product_name", nullable = false)
    private String productName;


    @Column(name = "description", length = 255)
    private String description; // 상품 설명

    @Column(name = "price", precision = 38, scale = 2, nullable = false)
    private BigDecimal price; // 상품 가격

    @Column(name = "stock", nullable = false)
    private Long stock; // 재고 수량

    @Column(name = "image_url") // 테이블의 image_url 컬럼과 매핑
    private String imageUrl; // 상품 이미지 URL

    @Column(name = "is_active") // 테이블의 is_active 컬럼과 매핑
    private Boolean isActive; // 상품 활성 상태

    @Column(name = "category")
    @Enumerated(EnumType.STRING) // Enum 타입을 문자열로 저장
    private ProductCategory category; // 상품 카테고리

    @Column(name = "created_at", updatable = false)
    @CreatedDate
    private LocalDateTime createdAt; // 생성시간

    @Column(name = "modified_at")
    @LastModifiedDate
    private LocalDateTime modifiedAt; // 수정시간


    public String getName() {
        return this.productName; // productName 반환
    }

    public void increaseStock(Long quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("수량은 0 보다 커야 합니다.");
        }

        // 현재 재고 상태와 추가할 수량을 로깅
        log.info("상품 재고 증가 시작 - 기존 재고: {}, 추가할 수량: {}", this.stock, quantity);

        // 기존 재고에 수량을 더함
        this.stock += quantity;

        // 재고 증가 후 상태를 로깅
        log.info("상품 재고 증가 완료 - 증가된 후 재고: {}", this.stock);
    }
}
