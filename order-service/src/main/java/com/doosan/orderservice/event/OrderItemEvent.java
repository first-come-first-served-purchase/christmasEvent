package com.doosan.orderservice.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OrderItemEvent {
    private Long productId; // 상품 아이디
    private Long quantity; // 수량
    private Long price; // 가격
}
