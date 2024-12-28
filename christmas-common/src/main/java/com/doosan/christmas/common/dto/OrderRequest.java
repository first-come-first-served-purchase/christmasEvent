package com.doosan.christmas.common.dto;

import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
public class OrderRequest {
    private List<OrderItemRequest> items;
    private LocalDateTime orderDate;
} 