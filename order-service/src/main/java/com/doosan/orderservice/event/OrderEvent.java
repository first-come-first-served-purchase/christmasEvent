package com.doosan.orderservice.event;

import com.doosan.orderservice.entity.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Date;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OrderEvent {
    private String eventType;  // 이벤트 타입
    private Long orderId; // 주문 ID
    private Long userId;   // 사용자 ID
    private Long totalAmount;  // 총 금액
    private PaymentStatus paymentStatus;   // 결제 상태
    private Date eventDate;  // 이벤트 발생 날짜
    private List<OrderItemEvent> items; // 주문 항목 목록
}