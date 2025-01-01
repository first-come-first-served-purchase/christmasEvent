package com.doosan.christmas.order.domain;

import com.doosan.christmas.order.shared.OrderStatus;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
@ToString
@Entity
@Table(name = "orders")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Order extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id")
    private Long userId;

    private Long productId;
    private Integer quantity;

    @Column(precision = 10, scale = 2)
    private BigDecimal totalPrice;

    @Column(name = "product_name", nullable = false)
    private String productName;

    @Enumerated(EnumType.STRING)
    private OrderStatus status;

    private LocalDateTime deliveryStartDate;
    private LocalDateTime deliveryCompleteDate;



    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL)
    private List<OrderItem> orderItems = new ArrayList<>();

    @Builder
    public Order(Long userId, Long productId, String productName, Integer quantity, BigDecimal totalPrice) {
        this.userId = userId;
        this.productId = productId;
        this.productName = productName;
        this.quantity = quantity;
        this.totalPrice = totalPrice;
        this.status = OrderStatus.ORDER_RECEIVED;
    }

    public void startDelivery() {
        this.status = OrderStatus.DELIVERING;
        this.deliveryStartDate = LocalDateTime.now();
    }

    public void completeDelivery() {
        this.status = OrderStatus.DELIVERY_COMPLETED;
        if (this.deliveryStartDate == null) {
            this.deliveryStartDate = LocalDateTime.now();
        }
        this.deliveryCompleteDate = LocalDateTime.now();
    }

    public boolean canCancel() {
        return this.status == OrderStatus.ORDER_RECEIVED;
    }

    public boolean canReturn() {
        return this.status == OrderStatus.DELIVERY_COMPLETED &&
                this.deliveryCompleteDate != null &&
                LocalDateTime.now().isBefore(this.deliveryCompleteDate.plusDays(1));
    }

    public void updateStatus(OrderStatus newStatus) {
        this.status = newStatus;
    }

    public void addOrderItem(OrderItem orderItem) {
        this.orderItems.add(orderItem);
    }

    public void addOrderItems(List<OrderItem> items) {
        this.orderItems.addAll(items);
    }
}
