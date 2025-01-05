package com.doosan.orderservice.service;

import com.doosan.common.dto.ResponseDto;
import com.doosan.common.dto.order.CreateOrderReqDto;
import com.doosan.orderservice.dto.CreateOrderResDto;
import com.doosan.orderservice.dto.OrderStatusUpdateRequest;
import org.springframework.http.ResponseEntity;

import java.util.List;

public interface OrderService {
    CreateOrderResDto createOrder(int userId, List<CreateOrderReqDto> orderItems);
    ResponseEntity<ResponseDto<Void>> cancelOrder(int userId, int orderId);
    ResponseEntity<ResponseDto<Void>> requestReturn(int userId, int orderId);
    ResponseEntity<?> updateOrderStatus(OrderStatusUpdateRequest request);
    void updateOrderStatus();
}
