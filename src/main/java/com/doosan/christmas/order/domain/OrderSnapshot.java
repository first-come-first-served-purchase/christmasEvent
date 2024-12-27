package com.doosan.christmas.order.domain;


import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;


@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OrderSnapshot {

    @Column(nullable = false)
    private Long productId;

    @Column(nullable = false)
    private String productName;

    @Column(nullable = false, length = 1000)
    private String description = "";

    @Column(nullable = false)
    private BigDecimal price;

    @Column
    private String imageUrl = "";

    @Column(nullable = false)
    private Long quantity = 0L;

    public OrderSnapshot(Long productId, String productName, String description,
                        BigDecimal price, String imageUrl) {
        this.productId = productId;
        this.productName = productName;
        this.description = description != null ? description : "";
        this.price = price;
        this.imageUrl = imageUrl != null ? imageUrl : "";
    }

    public void setQuantity(Long quantity) {
        this.quantity = quantity != null ? quantity : 0L;
    }
}
