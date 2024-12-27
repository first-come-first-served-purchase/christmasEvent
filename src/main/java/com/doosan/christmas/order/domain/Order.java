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

    @Embedded
    private OrderSnapshot orderSnapshot;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status = OrderStatus.ORDER_RECEIVED;

    @Column
    private LocalDateTime deliveryStartDate;

    @Column
    private LocalDateTime deliveryCompleteDate;

    @Builder
    public Order(Member member, Product product, Long quantity) {
        this.member = member;
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
