package com.doosan.orderservice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Date;

@Entity
@Table(name = "payment_history")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentHistory {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private Long orderId; // 주문 ID
    private Long userId; // 사용자 ID
    private Long amount; // 결제 총액
    
    @Enumerated(EnumType.STRING)
    private PaymentStatus status; // 결제 상태
    
    @Temporal(TemporalType.TIMESTAMP)
    private Date processedAt; // 처리 시간
} 