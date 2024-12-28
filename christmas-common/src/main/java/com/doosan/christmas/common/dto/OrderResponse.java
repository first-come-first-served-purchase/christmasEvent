package com.doosan.christmas.common.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.util.ArrayList;
import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderResponse {
    private List<OrderItemResponse> items;
    private int totalAmount;
    private int discountAmount;
    private int finalAmount;
    private List<String> appliedEvents;

    public static OrderResponse empty() {
        return OrderResponse.builder()
                .items(new ArrayList<>())
                .totalAmount(0)
                .discountAmount(0)
                .finalAmount(0)
                .appliedEvents(new ArrayList<>())
                .build();
    }
} 