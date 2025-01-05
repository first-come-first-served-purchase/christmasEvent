package com.doosan.orderservice.service;

import com.doosan.common.dto.order.CreateOrderReqDto;
import com.doosan.orderservice.dto.CreateOrderResDto;

import java.util.List;

public interface OrderService {
    CreateOrderResDto createOrder(int userId, List<CreateOrderReqDto> orderItems);
}
