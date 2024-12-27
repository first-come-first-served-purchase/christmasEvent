package com.doosan.christmas.order.dto.responseDto;

import com.doosan.christmas.order.domain.Order;
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
@Schema(description = "주문 응답 DTO")
public class OrderResponseDto {
    @Schema(description = "주문 ID")
    private Long orderId;

    @Schema(description = "상품명")
    private String productName;

    @Schema(description = "회원 ID")
    private Long memberId;

    @Schema(description = "회원 닉네임")
    private String memberNickname;

    @Schema(description = "상품 설명")
    @Builder.Default
    private String description = "";

    @Schema(description = "구매 가격")
    private BigDecimal price;

    @Schema(description = "주문 수량")
    @Builder.Default
    private Long quantity = 0L;

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

    @Schema(description = "배송 시작 상태")
    @Builder.Default
    private String deliveryStartStatus = "배송 전";

    @Schema(description = "배송 완료 상태")
    @Builder.Default
    private String deliveryCompleteStatus = "배송 전";


    public static OrderResponseDto from(Order order) {
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

        return OrderResponseDto.builder()
                .orderId(order.getId())
                .memberId(order.getMember().getId()) // memberId 추가
                .memberNickname(order.getMember().getNickname()) // nickname 추가
                .productName(order.getOrderSnapshot().getProductName())
                .description(order.getOrderSnapshot().getDescription())
                .price(order.getOrderSnapshot().getPrice())
                .quantity(order.getOrderSnapshot().getQuantity())
                .status(order.getStatus())
                .orderDate(order.getCreatedAt())
                .deliveryStartDate(order.getDeliveryStartDate())
                .deliveryCompleteDate(order.getDeliveryCompleteDate())
                .deliveryStartStatus(deliveryStartStatus)
                .deliveryCompleteStatus(deliveryCompleteStatus)
                .build();
    }
}