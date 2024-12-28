package com.doosan.christmas.common.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OrderItemRequest {
    private Long menuId;
    private int quantity;
} 