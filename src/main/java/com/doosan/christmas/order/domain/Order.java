package com.doosan.christmas.order.domain;

import com.doosan.christmas.member.domain.Member;
import com.doosan.christmas.product.domain.Product;
import com.doosan.christmas.order.shared.OrderStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "orders")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Order extends Timestamp {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false) // Product와 관계 추가
    private Product product;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "productId", column = @Column(name = "order_snapshot_product_id")),
            @AttributeOverride(name = "productName", column = @Column(name = "order_snapshot_product_name")),
            @AttributeOverride(name = "description", column = @Column(name = "order_snapshot_description")),
            @AttributeOverride(name = "price", column = @Column(name = "order_snapshot_price")),
            @AttributeOverride(name = "imageUrl", column = @Column(name = "order_snapshot_image_url")),
            @AttributeOverride(name = "quantity", column = @Column(name = "order_snapshot_quantity"))
    })
    private OrderSnapshot orderSnapshot = new OrderSnapshot(); // 기본값 추가



    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status = OrderStatus.ORDER_RECEIVED;

    @Column(name = "delivery_start_date")
    private LocalDateTime deliveryStartDate;

    @Column(name = "delivery_complete_date")
    private LocalDateTime deliveryCompleteDate;

    @Builder
    public Order(Member member, Product product, Long quantity) {
        this.member = member;
        this.product = product; // 관계 설정
        this.orderSnapshot = new OrderSnapshot(
                product.getId(),
                product.getName(),
                product.getDescription() != null ? product.getDescription() : "",
                product.getPrice(),
                product.getImageUrl() != null ? product.getImageUrl() : ""
        );
        this.orderSnapshot.setQuantity(quantity != null ? quantity : 0L);
        this.status = OrderStatus.ORDER_RECEIVED;
    }



    public void startDelivery() {
        this.status = OrderStatus.DELIVERING;
        this.deliveryStartDate = LocalDateTime.now();
    }

    public void completeDelivery() {
        // 배송 완료 상태로 변경
        this.status = OrderStatus.DELIVERY_COMPLETED;

        // 배송 시작일이 null이면 현재 시간으로 설정
        if (this.deliveryStartDate == null) {
            this.deliveryStartDate = LocalDateTime.now();
        }

        // 배송 완료일 설정
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
}
