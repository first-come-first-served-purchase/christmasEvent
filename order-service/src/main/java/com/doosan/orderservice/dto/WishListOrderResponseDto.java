package com.doosan.orderservice.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import java.util.Date;
import java.util.List;

@Getter
@Setter
@Builder
public class WishListOrderResponseDto {
    private int orderId; // 주문 아이디
    private int userId; // 유저 아이디
    private Date orderDate; // 주문 날짜
    private long totalPrice; // 총가격
    private List<OrderItemDto> items; // 담긴 상품들
} 