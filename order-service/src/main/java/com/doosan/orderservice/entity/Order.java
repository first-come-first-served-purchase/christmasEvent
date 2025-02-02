package com.doosan.orderservice.entity;

import com.doosan.common.entity.CommonEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.Date;

@Entity
@Table(name = "`order`")
@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class Order extends CommonEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id; // 주문 ID

    private int userId; // 사용자 ID

    private Date orderDate; // 주문 날짜

    private int totalPrice; // 총 주문 금액

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status = OrderStatus.ORDER_COMPLETE; // 주문 상태

    private Date deliveryStartDate;  // 배송 시작일

    private Date deliveryCompleteDate;  // 배송 완료일

    private Date returnRequestDate;  // 반품 신청일

    private Date returnCompleteDate;  // 반품 완료일

    @Column(name = "cancel_complete_date")
    private Date cancelCompleteDate; // 주문 취소 완료일

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status")
    private PaymentStatus paymentStatus; // 결제 진행 상태 (PAYMENT_PENDING, PAYMENT_COMPLETED, PAYMENT_FAILED)

    @Column(name = "payment_started_date")
    private Date paymentStartedDate; // 결제 시작 시간 (결제 화면 진입 시점)

    @Column(name = "payment_completed_date")
    private Date paymentCompletedDate; // 결제 완료 시간 (실제 결제 완료된 시점)

}
