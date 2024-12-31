package com.doosan.christmas.order.domain;

import com.doosan.christmas.order.shared.OrderStatus;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderSearchCondition {
    private OrderStatus status;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
}

