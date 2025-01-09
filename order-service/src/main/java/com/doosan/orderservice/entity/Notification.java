package com.doosan.orderservice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Date;

@Entity
@Table(name = "notifications")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Notification {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private Long userId; // 사용자 ID
    private Long orderId; // 주문 ID
    private String type; // 알림 타입
    private String message; // 알림 메시지
    
    @Temporal(TemporalType.TIMESTAMP)
    private Date sentAt; // 알림 전송 시간
} 