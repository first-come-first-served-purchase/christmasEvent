package com.doosan.orderservice.service;

import com.doosan.common.dto.ResponseDto;
import com.doosan.common.dto.order.CreateOrderReqDto;
import com.doosan.common.exception.BusinessRuntimeException;
import com.doosan.orderservice.dto.CreateOrderResDto;
import com.doosan.orderservice.dto.OrderStatusUpdateRequest;
import com.doosan.orderservice.entity.Order;
import com.doosan.orderservice.entity.OrderItem;
import com.doosan.orderservice.entity.OrderStatus;
import com.doosan.orderservice.repository.OrderItemRepository;
import com.doosan.orderservice.repository.OrderRepository;
import com.doosan.orderservice.repository.WishListRepository;
import com.doosan.productservice.dto.ProductResponse;
import com.doosan.productservice.service.ProductService;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;

@Service
@Slf4j
@RequiredArgsConstructor
public class OrderService {
    private final ProductService productService;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final WishListRepository wishListRepository;
    private final StockService stockService;
    private final ExecutorService executorService;

    @Transactional
    public CreateOrderResDto createOrder(int userId, List<CreateOrderReqDto> orderItems) {
        try {
            Order order = createAndSaveOrder(userId);
            int totalPrice = calculateTotalPrice(order, orderItems);
            updateOrderTotalPrice(order, totalPrice);
            return new CreateOrderResDto(order.getId(), userId, order.getOrderDate(), totalPrice);
        } catch (DataAccessException e) {
            log.error("데이터베이스 접근 중 오류 발생", e);
            throw new BusinessRuntimeException("주문 처리 중 데이터베이스 오류가 발생했습니다.", e);
        } catch (BusinessRuntimeException e) {
            log.error("주문 처리 중 비즈니스 로직 오류 발생", e);
            throw e;
        } catch (Exception e) {
            log.error("주문 처리 중 예기치 않은 오류 발생", e);
            throw new BusinessRuntimeException("주문 처리 중 예기치 않은 오류가 발생했습니다.", e);
        }
    }

    @Transactional
    public ResponseEntity<ResponseDto<Void>> cancelOrder(int userId, int orderId) {
        try {
            log.info("주문 취소 시작 - userId: {}, orderId: {}", userId, orderId);
            
            Order order = orderRepository.findById(orderId)
                    .orElseThrow(() -> new BusinessRuntimeException("주문을 찾을 수 없습니다."));
            log.info("주문 조회 완료 - 현재 상태: {}", order.getStatus());

            if (order.getUserId() != userId) {
                throw new BusinessRuntimeException("주문 취소 권한이 없습니다.");
            }

            if (order.getStatus() == OrderStatus.DELIVERING) {
                throw new BusinessRuntimeException("배송중인 주문은 취소할 수 없습니다.");
            }

            if (order.getStatus() == OrderStatus.CANCEL_COMPLETE) {
                throw new BusinessRuntimeException("이미 취소된 주문입니다.");
            }

            // 주문 상태 변경 및 취소 날짜 설정
            Date cancelDate = new Date();
            order.setStatus(OrderStatus.CANCEL_COMPLETE);
            order.setCancelCompleteDate(cancelDate);
            orderRepository.save(order);
            log.info("주문 상태 변경 완료 - 취소 날짜: {}", cancelDate);

            // 재고 복구
            List<OrderItem> orderItems = orderItemRepository.findByOrderId(orderId);
            for (OrderItem item : orderItems) {
                productService.updateStock(CreateOrderReqDto.builder()
                        .productId((long) item.getProductId())
                        .quantity((long) -item.getQuantity())
                        .build());
            }
            log.info("재고 복구 완료");

            // 변경된 주문 정보 다시 조회하여 확인
            Order updatedOrder = orderRepository.findById(orderId).orElseThrow();
            log.info("최종 주문 상태 - status: {}, cancelCompleteDate: {}", 
                    updatedOrder.getStatus(), updatedOrder.getCancelCompleteDate());

            return ResponseEntity.ok(
                    ResponseDto.<Void>builder()
                            .statusCode(HttpStatus.OK.value())
                            .resultMessage("주문이 성공적으로 취소되었습니다.")
                            .build()
            );
        } catch (BusinessRuntimeException e) {
            log.error("주문 취소 실패 - BusinessRuntimeException: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ResponseDto.<Void>builder()
                            .statusCode(HttpStatus.BAD_REQUEST.value())
                            .resultMessage(e.getMessage())
                            .build()
                    );
        } catch (Exception e) {
            log.error("주문 취소 실패 - Exception: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ResponseDto.<Void>builder()
                            .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                            .resultMessage("주문 취소 처리 중 오류가 발생했습니다.")
                            .build()
                    );
        }
    }

    @Transactional
    public ResponseEntity<ResponseDto<Void>> requestReturn(int userId, int orderId) {
        try {
            Order order = orderRepository.findById(orderId)
                    .orElseThrow(() -> new BusinessRuntimeException("주문을 찾을 수 없습니다."));

            if (order.getUserId() != userId) {
                throw new BusinessRuntimeException("주문 반품 권한이 없습니다.");
            }

            if (order.getStatus() != OrderStatus.DELIVERY_COMPLETE) {
                throw new BusinessRuntimeException("배송 완료된 주문만 반품 신청이 가능합니다.");
            }

            if (order.getReturnRequestDate() != null) {
                throw new BusinessRuntimeException("이미 반품 신청된 주문입니다.");
            }

            order.setStatus(OrderStatus.RETURN_REQUEST);
            order.setReturnRequestDate(new Date());
            orderRepository.save(order);

            return ResponseEntity.ok(
                    ResponseDto.<Void>builder()
                            .statusCode(HttpStatus.OK.value())
                            .resultMessage("반품 신청이 완료되었습니다.")
                            .build()
            );
        } catch (BusinessRuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(ResponseDto.<Void>builder()
                            .statusCode(HttpStatus.BAD_REQUEST.value())
                            .resultMessage(e.getMessage())
                            .build()
                    );
        }
    }

    @Transactional
    public ResponseEntity<ResponseDto<Void>> updateOrderStatus(OrderStatusUpdateRequest request) {
        try {
            Order order = orderRepository.findById(request.getOrderId())
                    .orElseThrow(() -> new BusinessRuntimeException("주문을 찾을 수 없습니다."));

            OrderStatus newStatus;
            try {
                newStatus = OrderStatus.valueOf(request.getStatus());
            } catch (IllegalArgumentException e) {
                throw new BusinessRuntimeException("잘못된 주문 상태입니다.");
            }

            // 상태별 날짜 업데이트
            switch (newStatus) {
                case DELIVERING:
                    order.setDeliveryStartDate(new Date());
                    break;
                case DELIVERY_COMPLETE:
                    order.setDeliveryCompleteDate(new Date());
                    break;
                case CANCEL_COMPLETE:
                    order.setCancelCompleteDate(new Date());
                    break;
                case RETURN_REQUEST:
                    order.setReturnRequestDate(new Date());
                    break;
                case RETURN_COMPLETE:
                    order.setReturnCompleteDate(new Date());
                    break;
            }

            order.setStatus(newStatus);
            orderRepository.save(order);

            return ResponseEntity.ok(
                    ResponseDto.<Void>builder()
                            .statusCode(HttpStatus.OK.value())
                            .resultMessage("주문 상태가 성공적으로 변경되었습니다.")
                            .build()
            );
        } catch (BusinessRuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(ResponseDto.<Void>builder()
                            .statusCode(HttpStatus.BAD_REQUEST.value())
                            .resultMessage(e.getMessage())
                            .build()
                    );
        }
    }

    private Order createAndSaveOrder(int userId) {
        Order order = new Order();
        order.setUserId(userId);
        order.setOrderDate(new Date());
        order.setStatus(OrderStatus.ORDER_COMPLETE);
        return orderRepository.save(order);
    }

    private int calculateTotalPrice(Order order, List<CreateOrderReqDto> orderItems) {
        int totalPrice = 0;
        for (CreateOrderReqDto item : orderItems) {
            totalPrice += processOrderItem(order.getId(), item);
        }
        return totalPrice;
    }

    private void updateOrderTotalPrice(Order order, int totalPrice) {
        order.setTotalPrice(totalPrice);
        orderRepository.save(order);
    }

    private int processOrderItem(int orderId, CreateOrderReqDto item) {
        OrderItem orderItem = createAndSaveOrderItem(orderId, item);
        return calculateItemPrice(item.getProductId(), item.getQuantity());
    }

    private OrderItem createAndSaveOrderItem(int orderId, CreateOrderReqDto item) {
        OrderItem orderItem = new OrderItem();
        orderItem.setOrderId(orderId);
        orderItem.setProductId(item.getProductId().intValue());
        orderItem.setQuantity(item.getQuantity().intValue());
        return orderItemRepository.save(orderItem);
    }

    private int calculateItemPrice(Long productId, Long quantity) {
        ResponseDto<ProductResponse> response = productService.getProduct(productId).getBody();
        if (response != null && response.getData() != null) {
            return (int) (response.getData().getPrice() * quantity.intValue());
        }
        throw new BusinessRuntimeException("상품 가격을 조회할 수 없습니다.");
    }

    @CircuitBreaker(name = "productService", fallbackMethod = "getProductFallback")
    public ProductResponse getProductWithCircuitBreaker(Long productId) {
        ResponseEntity<ResponseDto<ProductResponse>> response = productService.getProduct(productId);
        if (response.getBody() != null && response.getBody().getData() != null) {
            return response.getBody().getData();
        }
        throw new BusinessRuntimeException("상품 정보를 가져올 수 없습니다.");
    }



    @CircuitBreaker(name = "productService", fallbackMethod = "testRandomErrorFallback")
    public ProductResponse testRandomError() {
        ResponseEntity<ResponseDto<ProductResponse>> response = productService.testRandomError();
        if (response.getBody() != null && response.getBody().getData() != null) {
            return response.getBody().getData();
        }
        throw new BusinessRuntimeException("랜덤 에러 테스트 실패");
    }


    @CircuitBreaker(name = "productService", fallbackMethod = "testTimeoutFallback")
    public ProductResponse testTimeout() {
        ResponseEntity<ResponseDto<ProductResponse>> response = productService.testTimeout();
        if (response.getBody() != null && response.getBody().getData() != null) {
            return response.getBody().getData();
        }
        throw new BusinessRuntimeException("타임아웃 테스트 실패");
    }

}