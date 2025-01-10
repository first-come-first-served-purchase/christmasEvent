package com.doosan.orderservice.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationEvent {
    private String type; // 알림 타입
    private Long userId; // 사용자 id
    private Long orderId; // 주문 id
    private Long amount; // 총 액
    private String message; // 메시지
} 