package com.doosan.christmas.product.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // 상품 ID

    private String name; // 상품 이름

    private String description; // 상품 설명

    private BigDecimal price; // 상품 가격

    private Integer stock; // 재고 수량

    private String imageUrl; // 상품 이미지 URL

    private Boolean isActive; // 상품 활성 상태

    @Enumerated(EnumType.STRING) // Enum 타입을 문자열로 저장
    private ProductCategory category; // 상품 카테고리

    @CreatedDate
    private LocalDateTime createdAt; // 생성시간

    @LastModifiedDate
    private LocalDateTime modifiedAt; // 수정시간

}
