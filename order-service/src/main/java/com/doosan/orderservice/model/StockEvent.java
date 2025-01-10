package com.doosan.orderservice.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StockEvent {
    private StockEventType eventType; // 이벤트 타입
    private Long productId;// 상품 ID
    private Long quantity;  // 변경된 재고 수량
} 