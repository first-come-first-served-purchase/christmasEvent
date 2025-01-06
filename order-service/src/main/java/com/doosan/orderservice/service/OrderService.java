package com.doosan.orderservice.service;

import com.doosan.common.dto.ResponseDto;
import com.doosan.common.dto.order.CreateOrderReqDto;
import com.doosan.orderservice.dto.CreateOrderResDto;
import com.doosan.orderservice.dto.OrderStatusUpdateRequest;
import com.doosan.orderservice.dto.WishListDto;
import com.doosan.orderservice.dto.WishListOrderResponseDto;
import com.doosan.productservice.dto.ProductResponse;
import org.springframework.http.ResponseEntity;

import java.util.List;

public interface OrderService {
    CreateOrderResDto createOrder(int userId, List<CreateOrderReqDto> orderItems);
    ResponseEntity<ResponseDto<Void>> cancelOrder(int userId, int orderId);
    ResponseEntity<ResponseDto<Void>> requestReturn(int userId, int orderId);
    ResponseEntity<?> updateOrderStatus(OrderStatusUpdateRequest request);
    void updateOrderStatus();
    ResponseEntity<ResponseDto<List<WishListDto>>> getWishList(int userId);
    ResponseEntity<ResponseDto<Void>> addToWishList(int userId, Long productId, int quantity);
    ResponseEntity<ResponseDto<Void>> updateWishListItem(int userId, Long productId, int quantity);
    ResponseEntity<ResponseDto<Void>> removeFromWishList(int userId, Long productId);
    ResponseEntity<ResponseDto<WishListOrderResponseDto>> orderFromWishList(int userId, List<Long> productIds);

    ProductResponse getProductWithCircuitBreaker(Long productId);
    ProductResponse testRandomError();
    ProductResponse testTimeout();
}
