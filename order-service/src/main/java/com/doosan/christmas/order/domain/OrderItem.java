package com.doosan.christmas.order.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "order_items")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OrderItem extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private Order order;

    private Long productId;
    
    private String productName;
    
    private Integer quantity;
    
    @Column(precision = 10, scale = 2)
    private BigDecimal price;  // 상품 1개 가격
    
    @Column(precision = 10, scale = 2)
    private BigDecimal totalPrice;  // 수량 * 가격

    @Builder
    public OrderItem(Order order, Long productId, String productName, Integer quantity, BigDecimal price) {
        this.order = order;
        this.productId = productId;
        this.productName = productName;
        this.quantity = quantity;
        this.price = price;
        this.totalPrice = price.multiply(BigDecimal.valueOf(quantity));
    }
} 