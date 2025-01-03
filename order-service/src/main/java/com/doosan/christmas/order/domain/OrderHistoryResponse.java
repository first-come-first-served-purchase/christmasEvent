package com.doosan.christmas.order.domain;

import com.doosan.christmas.order.shared.OrderStatus;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Builder
@Schema(description = "주문 이력 응답")
public class OrderHistoryResponse {
    @Schema(description = "주문 ID")
    private Long orderId;

    @Schema(description = "회원 ID")
    private Long userId;

    @Schema(description = "상품 ID")
    private Long productId;

    @Schema(description = "상품명")
    private String productName;

    @Schema(description = "상품 설명")
    private String description;

    @Schema(description = "상품 가격")
    private BigDecimal price;

    @Schema(description = "주문 수량")
    private Integer quantity;

    @Schema(description = "주문 상태")
    private OrderStatus status;

    @Schema(description = "주문 일시")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime orderDate;

    @Schema(description = "배송 시작 일시")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime deliveryStartDate;

    @Schema(description = "배송 완료 일시")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime deliveryCompleteDate;

    public static OrderHistoryResponse from(Order order) {
        return OrderHistoryResponse.builder()
                .orderId(order.getId())
                .userId(order.getUserId())
                .productId(order.getProductId())
                .quantity(order.getQuantity())
                .price(order.getTotalPrice())
                .status(order.getStatus())
                .orderDate(order.getCreatedAt())
                .deliveryStartDate(order.getDeliveryStartDate())
                .deliveryCompleteDate(order.getDeliveryCompleteDate())
                .build();
    }
}