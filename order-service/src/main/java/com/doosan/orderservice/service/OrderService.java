package com.doosan.orderservice.service;

import com.doosan.common.dto.ResponseDto;
import com.doosan.common.dto.order.CreateOrderReqDto;
import com.doosan.common.exception.BusinessRuntimeException;
import com.doosan.orderservice.dto.CreateOrderResDto;
import com.doosan.orderservice.dto.OrderStatusUpdateRequest;
import com.doosan.orderservice.entity.Order;
import com.doosan.orderservice.entity.OrderItem;
import com.doosan.orderservice.entity.OrderStatus;
import com.doosan.orderservice.entity.PaymentStatus;
import com.doosan.orderservice.model.StockEvent;
import com.doosan.orderservice.model.StockEventType;
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
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

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
    private final OrderEventService orderEventService;
    private final ReactiveStockService reactiveStockService;
    private final ReactiveStockEventService reactiveStockEventService;

    // 주문 생성
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

    // 주문 취소
    @Transactional
    public Mono<ResponseEntity<ResponseDto<Void>>> cancelOrder(int userId, int orderId) {
        return Mono.fromCallable(() -> {
            log.info("주문 취소 시작 - userId: {}, orderId: {}", userId, orderId);
            
            return orderRepository.findById(orderId)
                    .map(order -> {
                        if (order.getUserId() != userId) {
                            throw new BusinessRuntimeException("주문 취소 권한이 없습니다.");
                        }
                        
                        if (order.getStatus() == OrderStatus.DELIVERING) {
                            throw new BusinessRuntimeException("배송중인 주문은 취소할 수 없습니다.");
                        }
                        
                        if (order.getStatus() == OrderStatus.CANCEL_COMPLETE) {
                            throw new BusinessRuntimeException("이미 취소된 주문입니다.");
                        }
                        
                        order.setStatus(OrderStatus.CANCEL_COMPLETE);
                        orderRepository.save(order);
                        
                        return ResponseEntity.ok(
                            ResponseDto.<Void>builder()
                                .statusCode(HttpStatus.OK.value())
                                .resultMessage("주문 취소가 완료되었습니다.")
                                .build()
                        );
                    })
                    .orElseThrow(() -> new BusinessRuntimeException("주문을 찾을 수 없습니다."));
        })
        .subscribeOn(Schedulers.boundedElastic())
        .onErrorResume(BusinessRuntimeException.class, 
            e -> Mono.just(ResponseEntity.badRequest()
                .body(ResponseDto.<Void>builder()
                    .statusCode(HttpStatus.BAD_REQUEST.value())
                    .resultMessage(e.getMessage())
                    .build()
                )));
    }

    // 주문 반품
    @Transactional
    public Mono<ResponseEntity<ResponseDto<Void>>> requestReturn(int userId, int orderId) {
        return Mono.fromCallable(() -> {
            log.info("반품 요청 시작 - userId: {}, orderId: {}", userId, orderId);
            
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
        })
        .subscribeOn(Schedulers.boundedElastic())
        .onErrorResume(BusinessRuntimeException.class, 
            e -> Mono.just(ResponseEntity.badRequest()
                .body(ResponseDto.<Void>builder()
                    .statusCode(HttpStatus.BAD_REQUEST.value())
                    .resultMessage(e.getMessage())
                    .build()
                )));
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
        OrderItem orderItem = null;
        int itemTotalPrice = calculateItemPrice(item.getProductId(), item.getQuantity());

        try {
            // ProductService 재고 차감
            productService.updateStock(CreateOrderReqDto.builder()
                    .productId(item.getProductId())
                    .quantity(-item.getQuantity())
                    .build());

            // 주문 아이템 생성 (계산된 총 금액 사용)
            orderItem = OrderItem.builder()
                    .orderId(orderId)
                    .productId(item.getProductId().intValue())
                    .quantity(item.getQuantity().intValue())
                    .price(itemTotalPrice)  // 계산된 총 금액 저장
                    .build();
            orderItemRepository.save(orderItem);

            // 결제 완료 상태로 변경
            Order order = orderRepository.findById(orderId)
                    .orElseThrow(() -> new BusinessRuntimeException("주문을 찾을 수 없습니다."));
            order.setPaymentStatus(PaymentStatus.PAYMENT_COMPLETED);
            order.setPaymentCompletedDate(new Date());
            order.setTotalPrice(itemTotalPrice); // 주문 총액 설정
            orderRepository.save(order);

            // 결제 완료 이벤트 발행
            orderEventService.publishPaymentCompletedEvent(order, List.of(orderItem));

            return itemTotalPrice;
        } catch (Exception e) {
            // 실패시 Redis 재고 복구 및 결제 실패 상태로 변경
            stockService.restoreStock(item.getProductId(), item.getQuantity());
            Order order = orderRepository.findById(orderId)
                    .orElseThrow(() -> new BusinessRuntimeException("주문을 찾을 수 없습니다."));
            order.setPaymentStatus(PaymentStatus.PAYMENT_FAILED);
            orderRepository.save(order);
            
            // 결제 실패 이벤트 발행
            if (orderItem != null) {
                orderEventService.publishPaymentFailedEvent(order, List.of(orderItem));
            }
            throw e;
        }
    }

    private OrderItem createAndSaveOrderItem(int orderId, CreateOrderReqDto item) {
        // 상품 가격 조회
        ResponseDto<ProductResponse> response = productService.getProduct(item.getProductId()).getBody();
        int price = 0;
        if (response != null && response.getData() != null) {
            price = Math.toIntExact(response.getData().getPrice());
        }

        OrderItem orderItem = new OrderItem();
        orderItem.setOrderId(orderId);
        orderItem.setProductId(item.getProductId().intValue());
        orderItem.setQuantity(item.getQuantity().intValue());
        orderItem.setPrice(price);  // 가격 설정
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

    // 결제 상태 조회 메서드 추가
    public PaymentStatus getOrderPaymentStatus(int orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new BusinessRuntimeException("주문을 찾을 수 없습니다."));
        log.info("주문 결제 상태 조회 - 주문 ID: {}, 결제 상태: {}", orderId, order.getPaymentStatus());
        return order.getPaymentStatus();
    }

    public Mono<CreateOrderResDto> createReactiveOrder(int userId, List<CreateOrderReqDto> orderRequests) {
        return Flux.fromIterable(orderRequests)
            .flatMap(request -> 
                reactiveStockService.checkAndReduceStock(
                    request.getProductId(), 
                    request.getQuantity()
                )
                .map(success -> {
                    if (!success) {
                        throw new BusinessRuntimeException("재고 부족");
                    }
                    return request;
                })
            )
            .collectList()
            .flatMap(validatedRequests -> 
                Mono.fromCallable(() -> {
                    CreateOrderResDto orderResult = createOrder(userId, validatedRequests);
                    reactiveStockEventService.publishStockEvent(new StockEvent(
                        StockEventType.STOCK_REDUCED,
                        Long.valueOf(orderResult.getOrderId()),
                        Long.valueOf(orderResult.getTotalPrice())
                    )).subscribe();
                    return orderResult;
                })
            );
    }

}


