package com.doosan.christmas.order.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Embeddable
@Getter
@Table(name = "order_snapshot") // 테이블 이름 지정
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OrderSnapshot {

    @Column(name = "product_id", nullable = false) // 테이블의 product_id 컬럼과 매핑
    private Long productId;

    @Column(name = "product_name", nullable = false) // 테이블의 product_name 컬럼과 매핑
    private String productName;

    @Column(nullable = false, length = 1000)
    private String description = "";

    @Column(nullable = false)
    private BigDecimal price;

    @Column(name = "image_url") // 테이블의 image_url 컬럼과 매핑
    private String imageUrl = "";

    @Column(nullable = false)
    private Long quantity = 0L;

    @Builder
    public OrderSnapshot(Long productId, String productName, String description,
                         BigDecimal price, String imageUrl, Long quantity) {
        this.productId = productId;
        this.productName = productName;
        this.description = description != null ? description : "";
        this.price = price;
        this.imageUrl = imageUrl != null ? imageUrl : "";
        this.quantity = quantity != null ? quantity : 0L;
    }
}
