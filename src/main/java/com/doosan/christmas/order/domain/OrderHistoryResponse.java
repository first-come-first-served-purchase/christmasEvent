package com.doosan.christmas.order.domain;

import com.doosan.christmas.order.shared.OrderStatus;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "주문 이력 응답 DTO")
public class OrderHistoryResponse {

    @Schema(description = "회원 ID")
    private Long memberId;

    @Schema(description = "주문 ID")
    private Long orderId;

    @Schema(description = "상품 ID")
    private Long productId;

    @Schema(description = "상품명")
    private String productName;

    @Schema(description = "상품 설명")
    @Builder.Default
    private String description = "";

    @Schema(description = "구매 가격")
    private BigDecimal price;

    @Schema(description = "상품 이미지")
    @Builder.Default
    private String imageUrl = "";

    @Schema(description = "주문 수량")
    @Builder.Default
    private Long quantity = 0L;

    @Schema(description = "주문 상태")
    private OrderStatus status;

    @Schema(description = "주문 일시")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime orderDate;

    @Schema(description = "배송 시작 일시", example = "진행 전")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime deliveryStartDate;

    @Schema(description = "배송 완료 일시", example = "진행 전")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime deliveryCompleteDate;

    @Schema(description = "배송 시작 상태")
    private String deliveryStartStatus;

    @Schema(description = "배송 완료 상태")
    private String deliveryCompleteStatus;



    public static OrderHistoryResponse from(Order order) {
        OrderSnapshot snapshot = order.getOrderSnapshot();
        
        String deliveryStartStatus = "배송 전";
        String deliveryCompleteStatus = "배송 전";
        
        if (order.getStatus() == OrderStatus.DELIVERING || 
            order.getStatus() == OrderStatus.DELIVERY_COMPLETED) {
            deliveryStartStatus = "배송 시작";
        }
        
        if (order.getStatus() == OrderStatus.DELIVERY_COMPLETED) {
            deliveryCompleteStatus = "배송 완료";
        }
        
        if (order.getStatus() == OrderStatus.CANCELLED) {
            deliveryStartStatus = "주문 취소";
            deliveryCompleteStatus = "주문 취소";
        }
        
        if (order.getStatus() == OrderStatus.RETURN_REQUESTED || 
            order.getStatus() == OrderStatus.RETURN_COMPLETED) {
            deliveryStartStatus = "반품 진행 중";
            deliveryCompleteStatus = "반품 진행 중";
        }

        // memberId를 설정위해 order.getMember()에서 memberId를 가져 온다.
        Long memberId = (order.getMember() != null) ? order.getMember().getId() : null;

        return OrderHistoryResponse.builder()
                .orderId(order.getId())
                .productId(snapshot.getProductId())

                .memberId(memberId)

                .productName(snapshot.getProductName())

                .description(snapshot.getDescription())
                .price(snapshot.getPrice())
                .imageUrl(snapshot.getImageUrl() != null ? snapshot.getImageUrl() : "")
                .quantity(snapshot.getQuantity() != null ? snapshot.getQuantity() : 0L)
                .status(order.getStatus())
                .orderDate(order.getCreatedAt())

                .deliveryStartDate(order.getDeliveryStartDate())
                .deliveryCompleteDate(order.getDeliveryCompleteDate())
                .deliveryStartStatus(deliveryStartStatus)
                .deliveryCompleteStatus(deliveryCompleteStatus)
                .build();
    }
}