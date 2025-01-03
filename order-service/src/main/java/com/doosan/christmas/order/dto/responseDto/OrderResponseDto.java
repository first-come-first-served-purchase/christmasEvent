package com.doosan.christmas.order.dto.responseDto;

import com.doosan.christmas.order.domain.Order;
import com.doosan.christmas.order.shared.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderResponseDto {
    private Long id;
    private Long userId;
    private Long productId;
    private Integer quantity;
    private BigDecimal totalPrice;
    private OrderStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime modifiedAt;

    public static OrderResponseDto from(Order order) {
        return OrderResponseDto.builder()
                .id(order.getId())
                .userId(order.getUserId())
                .productId(order.getProductId())
                .quantity(order.getQuantity())
                .totalPrice(order.getTotalPrice())
                .status(order.getStatus())
                .createdAt(order.getCreatedAt())
                .modifiedAt(order.getModifiedAt())
                .build();
    }
}