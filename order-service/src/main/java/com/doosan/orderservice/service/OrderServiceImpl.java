package com.doosan.orderservice.service;

import com.doosan.common.dto.ResponseDto;
import com.doosan.common.dto.order.CreateOrderReqDto;
import com.doosan.common.exception.BusinessRuntimeException;
import com.doosan.orderservice.dto.*;
import com.doosan.orderservice.entity.Order;
import com.doosan.orderservice.entity.OrderItem;
import com.doosan.orderservice.entity.OrderStatus;
import com.doosan.orderservice.entity.WishList;
import com.doosan.orderservice.repository.OrderItemRepository;
import com.doosan.orderservice.repository.OrderRepository;
import com.doosan.orderservice.repository.WishListRepository;
import com.doosan.productservice.dto.ProductResponse;
import com.doosan.productservice.service.ProductService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;

import java.util.Date;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Service
@RequiredArgsConstructor
@Log4j2
public class OrderServiceImpl implements OrderService {

    private final ProductService productService;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final WishListRepository wishListRepository;

    // 주문 하기
    @Override
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

    // 주문 생성 , 저장
    private Order createAndSaveOrder(int userId) {
        Order order = new Order();
        order.setUserId(userId);
        order.setOrderDate(new Date());
        order.setTotalPrice(0);

        return saveOrder(order);
    }

    // 주문 저장
    private Order saveOrder(Order order) {
        try {
            return orderRepository.save(order);
        } catch (DataAccessException e) {
            log.error("주문 저장 중 데이터베이스 오류 발생", e);
            throw new BusinessRuntimeException("주문 저장에 실패했습니다.", e);
        } catch (Exception e) {
            log.error("주문 저장 중 예기치 않은 오류 발생", e);
            throw new BusinessRuntimeException("주문 저장 중 예기치 않은 오류가 발생했습니다.", e);
        }
    }

    // 주문 아이템의 총 금액을 계산
    private int calculateTotalPrice(Order order, List<CreateOrderReqDto> orderItems) {
        List<CompletableFuture<Integer>> futureList = orderItems.stream()
                .map(item -> CompletableFuture.supplyAsync(() -> processOrderItem(order.getId(), item)))
                .toList();

        try {
            return futureList.stream()
                    .mapToInt(future -> {
                        try {
                            return future.get(30, TimeUnit.SECONDS);
                        } catch (InterruptedException | ExecutionException | TimeoutException e) {
                            log.error("주문 가격 계산 실패", e);
                            throw new BusinessRuntimeException("주문 가격 계산에 실패했습니다.", e);
                        }
                    })
                    .sum();
        } catch (BusinessRuntimeException e) {
            throw e;
        }
    }

    // 주문 아이템 처리
    private int processOrderItem(int orderId, CreateOrderReqDto item) {
        OrderItem orderItem = createAndSaveOrderItem(orderId, item);

        // 가격 추출
        Long productPrice = productService.getProductPrice(item.getProductId())
                .getBody()
                .getData(); // 데이터 추출

        if (productPrice == null) {
            throw new BusinessRuntimeException("상품 가격을 조회할 수 없습니다. 상품 ID: " + item.getProductId());
        }

        updateProductStock(item);

        return Math.toIntExact(productPrice * item.getQuantity());
    }



    // 주문한 상품의 재고 업데이트
    private void updateProductStock(CreateOrderReqDto quantity) {
        try {
            productService.updateStock(quantity);
        } catch (BusinessRuntimeException e) {
            log.error("재고 업데이트 중 오류 발생", e);
            throw e;
        } catch (Exception e) {
            log.error("재고 업데이트 중 예기치 않은 오류 발생", e);
            throw new BusinessRuntimeException("재고 업데이트 중 예기치 않은 오류가 발생했습니다.", e);
        }
    }

    // 주문 항목 생성 및 저장
    private OrderItem createAndSaveOrderItem(int orderId, CreateOrderReqDto item) {
        OrderItem orderItem = new OrderItem();
        orderItem.setOrderId(orderId);
        orderItem.setProductId(Math.toIntExact(item.getProductId()));
        orderItem.setQuantity(Math.toIntExact(item.getQuantity()));

        return saveOrderItem(orderItem);
    }

    // 주문 항목 저장
    private OrderItem saveOrderItem(OrderItem orderItem) {
        try {
            return orderItemRepository.save(orderItem);
        } catch (DataAccessException e) {
            log.error("주문 항목 저장 중 데이터베이스 오류 발생", e);
            throw new BusinessRuntimeException("주문 항목 저장에 실패했습니다.", e);
        } catch (Exception e) {
            log.error("주문 항목 저장 중 예기치 않은 오류 발생", e);
            throw new BusinessRuntimeException("주문 항목 저장 중 예기치 않은 오류가 발생했습니다.", e);
        }
    }

    // 주문 TotalPrice 업데이트
    private void updateOrderTotalPrice(Order order, int totalPrice) {
        try {
            order.setTotalPrice(totalPrice);
            orderRepository.save(order);
        } catch (DataAccessException e) {
            log.error("주문 가격 업데이트 중 데이터베이스 오류 발생", e);
            throw new BusinessRuntimeException("주문 가격 업데이트에 실패했습니다.", e);
        } catch (Exception e) {
            log.error("주문 가격 업데이트 중 예기치 않은 오류 발생", e);
            throw new BusinessRuntimeException("주문 가격 업데이트 중 예기치 않은 오류가 발생했습니다.", e);
        }
    }

    // 주문 취소
    @Override
    @Transactional
    public ResponseEntity<ResponseDto<Void>> cancelOrder(int userId, int orderId) {
        try {
            Order order = orderRepository.findById(orderId)
                    .orElseThrow(() -> new BusinessRuntimeException("주문을 찾을 수 없습니다."));

            // 권한 확인
            if (order.getUserId() != userId) {
                throw new BusinessRuntimeException("주문 취소 권한이 없습니다.");
            }

            // 배송중 상태 확인
            if (order.getStatus() == OrderStatus.DELIVERING) {
                throw new BusinessRuntimeException("배송중인 주문은 취소할 수 없습니다.");
            }

            // 이미 취소된 주문인지 확인
            if (order.getStatus() == OrderStatus.CANCEL_COMPLETE) {
                throw new BusinessRuntimeException("이미 취소된 주문입니다.");
            }

            // 주문 상태 변경
            order.setStatus(OrderStatus.CANCEL_COMPLETE);
            orderRepository.save(order);

            // 재고 복구
            List<OrderItem> orderItems = orderItemRepository.findByOrderId(orderId);
            for (OrderItem item : orderItems) {
                productService.updateStock(CreateOrderReqDto.builder()
                        .productId((long) item.getProductId())
                        .quantity((long) -item.getQuantity()) // 음수로 설정하여 재고 증가
                        .build());
            }

            return ResponseEntity.ok(
                    ResponseDto.<Void>builder()
                            .statusCode(HttpStatus.OK.value())
                            .resultMessage("주문이 성공적으로 취소되었습니다.")
                            .build()
            );
        } catch (BusinessRuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(ResponseDto.<Void>builder()
                            .statusCode(HttpStatus.BAD_REQUEST.value())
                            .resultMessage("주문 취소 실패")
                            .detailMessage(e.getMessage())
                            .build()
                    );
        }
    }

    // 반품 신청
    @Override
    @Transactional
    public ResponseEntity<ResponseDto<Void>> requestReturn(int userId, int orderId) {
        try {
            Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new EntityNotFoundException("주문을 찾을 수 없습니다."));

            // 권한 확인
            if (order.getUserId() != userId) {
                throw new BusinessRuntimeException("반품 신청 권한이 없습니다.");
            }

            // 배송완료 상태 확인
            if (order.getStatus() != OrderStatus.DELIVERY_COMPLETE) {
                throw new BusinessRuntimeException("배송완료된 상품만 반품 신청이 가능합니다.");
            }

            // 반송완료일자 확인
            if (order.getDeliveryCompleteDate() == null) {
                throw new BusinessRuntimeException("배송완료일자가 없는 주문입니다.");
            }

            // 반품 가능 기간 확인 (배송완료 후 D+1까지)
            Date now = new Date();
            Date returnLimit = new Date(order.getDeliveryCompleteDate().getTime() + 24*60*60*1000);
            if (now.after(returnLimit)) {
                throw new BusinessRuntimeException("반품 신청 기간이 만료되었습니다.");
            }

            order.setStatus(OrderStatus.RETURN_REQUEST);
            order.setReturnRequestDate(now);
            orderRepository.save(order);

            return ResponseEntity.ok(
                ResponseDto.<Void>builder()
                    .statusCode(HttpStatus.OK.value())
                    .resultMessage("반품이 성공적으로 신청되었습니다.")
                    .build()
            );
        } catch (BusinessRuntimeException e) {
            return ResponseEntity.badRequest()
                .body(ResponseDto.<Void>builder()
                    .statusCode(HttpStatus.BAD_REQUEST.value())
                    .resultMessage(e.getMessage())
                    .build()
                );
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ResponseDto.<Void>builder()
                    .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .resultMessage("반품 신청 처리 중 오류가 발생했습니다.")
                    .build()
                );
        }
    }

    // 매일 자정에 스케줄링 , 주문 상태 업데이트
    @Scheduled(cron = "0 0 0 * * *") // 매일 자정에 실행
    @Override
    @Transactional
    public void updateOrderStatus() {
        Date now = new Date();
        List<Order> orders = orderRepository.findAll();

        for (Order order : orders) {
            switch (order.getStatus()) {
                case ORDER_COMPLETE:
                    // 주문완료 후 D+1에 배송중으로 변경
                    if (isDatePassed(order.getOrderDate(), 1)) {
                        order.setStatus(OrderStatus.DELIVERING);
                        order.setDeliveryStartDate(now);
                    }
                    break;

                case DELIVERING:
                    // 배송시작 후 D+1에 배송완료로 변경
                    if (isDatePassed(order.getDeliveryStartDate(), 1)) {
                        order.setStatus(OrderStatus.DELIVERY_COMPLETE);
                        order.setDeliveryCompleteDate(now);
                    }
                    break;

                case RETURN_REQUEST:
                    // 반품신청 후 D+1에 반품완료로 변경 및 재고 반영
                    if (isDatePassed(order.getReturnRequestDate(), 1)) {
                        order.setStatus(OrderStatus.RETURN_COMPLETE);
                        order.setReturnCompleteDate(now);

                        // 재고 복구
                        List<OrderItem> orderItems = orderItemRepository.findByOrderId(order.getId());
                        for (OrderItem item : orderItems) {
                            productService.updateStock(CreateOrderReqDto.builder()
                                    .productId((long) item.getProductId())
                                    .quantity((long) -item.getQuantity()) // 음수로 설정하여 재고 증가
                                    .build());
                        }
                    }
                    break;
            }
        }
        orderRepository.saveAll(orders);
    }

    private boolean isDatePassed(Date baseDate, int days) {
        if (baseDate == null) return false;
        Date now = new Date();
        Date compareDate = new Date(baseDate.getTime() + days * 24 * 60 * 60 * 1000);
        return now.after(compareDate);
    }

    // 주문 상태 업데이트
    @Override
    public ResponseEntity<?> updateOrderStatus(OrderStatusUpdateRequest request) {
        try {
            Order order = orderRepository.findById(request.getOrderId())
                .orElseThrow(() -> new EntityNotFoundException("주문을 찾을 수 없습니다."));
            
            OrderStatus newStatus = OrderStatus.valueOf(request.getStatus());
            order.setStatus(newStatus);
            
            Date now = new Date();
            
            // 각 상태별 날짜 설정
            switch (newStatus) {
                case DELIVERING:
                    order.setDeliveryStartDate(now);
                    break;
                case DELIVERY_COMPLETE:
                    order.setDeliveryCompleteDate(now);
                    break;
                case RETURN_REQUEST:
                    order.setReturnRequestDate(now);
                    break;
                case RETURN_COMPLETE:
                    order.setReturnCompleteDate(now);
                    break;
            }
            
            orderRepository.save(order);
            
            return ResponseEntity.ok(
                ResponseDto.<Void>builder()
                    .statusCode(HttpStatus.OK.value())
                    .resultMessage("주문 상태가 성공적으로 변경되었습니다.")
                    .build()
            );
        } catch (Exception e) {
            log.error("주문 상태 변경 실패: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ResponseDto.<Void>builder()
                    .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .resultMessage("주문 상태 변경 실패")
                    .detailMessage(e.getMessage())
                    .build()
                );
        }
    }

    // 위시리스트 조회
    @Override
    public ResponseEntity<ResponseDto<List<WishListDto>>> getWishList(int userId) {
        try {
            List<WishList> wishListItems = wishListRepository.findByUserIdAndIsDeletedFalse(userId);
            List<WishListDto> wishListDtos = wishListItems.stream()
                .map(item -> {
                    WishListDto dto = new WishListDto();
                    dto.setProductId(item.getProductId());
                    dto.setQuantity(item.getQuantity());
                    
                    ResponseDto<ProductResponse> productResponse = productService.getProduct(item.getProductId()).getBody();
                    if (productResponse != null && productResponse.getData() != null) {
                        ProductResponse product = productResponse.getData();
                        dto.setProductName(product.getName());
                        dto.setPrice(product.getPrice());
                        dto.setDescription(product.getDescription());
                    }
                    
                    return dto;
                })
                .toList();

            return ResponseEntity.ok(
                ResponseDto.<List<WishListDto>>builder()
                    .statusCode(HttpStatus.OK.value())
                    .resultMessage("위시리스트 조회 성공")
                    .data(wishListDtos)
                    .build()
            );
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ResponseDto.<List<WishListDto>>builder()
                    .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .resultMessage("위시리스트 조회 실패")
                    .build()
                );
        }
    }

    // 위시 리스트에 상품 추가
    @Override
    public ResponseEntity<ResponseDto<Void>> addToWishList(int userId, Long productId, int quantity) {
        try {
            if (wishListRepository.existsByUserIdAndProductIdAndIsDeletedFalse(userId, productId)) {
                throw new BusinessRuntimeException("이미 위시리스트에 존재하는 상품입니다.");
            }

            WishList wishList = new WishList();
            wishList.setUserId(userId);
            wishList.setProductId(productId);
            wishList.setQuantity(quantity);
            wishList.setDeleted(false);
            
            wishListRepository.save(wishList);

            return ResponseEntity.ok(
                ResponseDto.<Void>builder()
                    .statusCode(HttpStatus.OK.value())
                    .resultMessage("위시리스트에 상품이 추가되었습니다.")
                    .build()
            );
        } catch (BusinessRuntimeException e) {
            return ResponseEntity.badRequest()
                .body(ResponseDto.<Void>builder()
                    .statusCode(HttpStatus.BAD_REQUEST.value())
                    .resultMessage(e.getMessage())
                    .build()
                );
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ResponseDto.<Void>builder()
                    .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .resultMessage("위시리스트 추가 실패")
                    .build()
                );
        }
    }

    // 위시리스트 상품 수량 변경
    @Override
    public ResponseEntity<ResponseDto<Void>> updateWishListItem(int userId, Long productId, int quantity) {
        try {
            WishList wishList = wishListRepository.findByUserIdAndProductIdAndIsDeletedFalse(userId, productId)
                .orElseThrow(() -> new BusinessRuntimeException("위시리스트에 존재하지 않는 상품입니다."));

            wishList.setQuantity(quantity);
            wishListRepository.save(wishList);

            return ResponseEntity.ok(
                ResponseDto.<Void>builder()
                    .statusCode(HttpStatus.OK.value())
                    .resultMessage("위시리스트 상품 수량이 수정되었습니다.")
                    .build()
            );
        } catch (BusinessRuntimeException e) {
            return ResponseEntity.badRequest()
                .body(ResponseDto.<Void>builder()
                    .statusCode(HttpStatus.BAD_REQUEST.value())
                    .resultMessage(e.getMessage())
                    .build()
                );
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ResponseDto.<Void>builder()
                    .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .resultMessage("위시리스트 수정 실패")
                    .build()
                );
        }
    }

    // 위시리스트에서 상품 제거
    @Override
    public ResponseEntity<ResponseDto<Void>> removeFromWishList(int userId, Long productId) {
        try {
            WishList wishList = wishListRepository.findByUserIdAndProductIdAndIsDeletedFalse(userId, productId)
                .orElseThrow(() -> new BusinessRuntimeException("위시리스트에 존재하지 않는 상품입니다."));

            wishList.setDeleted(true);
            wishListRepository.save(wishList);

            return ResponseEntity.ok(
                ResponseDto.<Void>builder()
                    .statusCode(HttpStatus.OK.value())
                    .resultMessage("위시리스트에서 상품이 제거되었습니다.")
                    .build()
            );
        } catch (BusinessRuntimeException e) {
            return ResponseEntity.badRequest()
                .body(ResponseDto.<Void>builder()
                    .statusCode(HttpStatus.BAD_REQUEST.value())
                    .resultMessage(e.getMessage())
                    .build()
                );
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ResponseDto.<Void>builder()
                    .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .resultMessage("위시리스트 제거 실패")
                    .build()
                );
        }
    }

    // 위시리스트에 있는 상품을 주문
    @Override
    public ResponseEntity<ResponseDto<WishListOrderResponseDto>> orderFromWishList(int userId, List<Long> productIds) {
        try {
            List<WishList> wishListItems = wishListRepository.findByUserIdAndIsDeletedFalse(userId)
                .stream()
                .filter(item -> productIds.contains(item.getProductId()))
                .toList();

            if (wishListItems.isEmpty()) {
                throw new BusinessRuntimeException("주문할 상품이 위시리스트에 없습니다.");
            }

            // 주문 생성을 위한 요청 아이템 리스트 생성
            List<CreateOrderReqDto> orderItemRequests = wishListItems.stream()
                .map(item -> CreateOrderReqDto.builder()
                    .productId(item.getProductId())
                    .quantity((long) item.getQuantity())
                    .build())
                .toList();

            // 주문 생성 및 결과 저장
            CreateOrderResDto orderResult = createOrder(userId, orderItemRequests);
            
            // Order 엔티티 조회
            Order createdOrder = orderRepository.findById(orderResult.getOrderId())
                .orElseThrow(() -> new BusinessRuntimeException("생성된 주문을 찾을 수 없습니다."));

            // 위시리스트 아이템 삭제 처리
            wishListItems.forEach(item -> item.setDeleted(true));
            wishListRepository.saveAll(wishListItems);

            // 주문 아이템 조회
            List<OrderItem> orderedItems = orderItemRepository.findByOrderId(createdOrder.getId());
            
            // 응답 데이터 생성
            WishListOrderResponseDto responseData = WishListOrderResponseDto.builder()
                .orderId(createdOrder.getId())
                .userId(userId)
                .orderDate(createdOrder.getOrderDate())
                .totalPrice(createdOrder.getTotalPrice())
                .items(orderedItems.stream()
                    .<OrderItemDto>map(item -> {
                        ProductResponse productResponse = getProductWithCircuitBreaker(Long.valueOf(item.getProductId())); // 서킷브레이커 추가
                        return OrderItemDto.builder()
                            .productId(Long.valueOf(item.getProductId()))
                            .productName(productResponse.getName())
                            .quantity(item.getQuantity())
                            .price(productResponse.getPrice())
                            .build();
                    })
                    .toList())
                .build();

            return ResponseEntity.ok(
                ResponseDto.<WishListOrderResponseDto>builder()
                    .statusCode(HttpStatus.OK.value())
                    .resultMessage("위시리스트 상품 주문이 완료되었습니다.")
                    .data(responseData)
                    .build()
            );

        } catch (BusinessRuntimeException e) {
            return ResponseEntity.badRequest()
                .body(ResponseDto.<WishListOrderResponseDto>builder()
                    .statusCode(HttpStatus.BAD_REQUEST.value())
                    .resultMessage(e.getMessage())
                    .build()
                );
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ResponseDto.<WishListOrderResponseDto>builder()
                    .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .resultMessage("위시리스트 주문 실패")
                    .build()
                );
        }
    }

    @Retry(name = "productService", fallbackMethod = "getProductFallback")
    @CircuitBreaker(name = "productService", fallbackMethod = "getProductFallback")
    public ProductResponse getProductWithCircuitBreaker(Long productId) {
        ResponseEntity<ResponseDto<ProductResponse>> response = productService.getProduct(productId);
        if (response.getBody() == null || response.getBody().getData() == null) {
            throw new BusinessRuntimeException("상품 정보를 조회할 수 없습니다.");
        }
        return response.getBody().getData();
    }

    private ProductResponse getProductFallback(Long productId, Exception e) {
        log.error("상품 정보 조회 실패. ProductId: {}, Error: {}", productId, e.getMessage());
        throw new BusinessRuntimeException("일시적인 서비스 장애로 상품 정보를 조회할 수 없습니다.");
    }

    @Retry(name = "productService", fallbackMethod = "getProductFallback")
    @CircuitBreaker(name = "productService", fallbackMethod = "getProductFallback")
    public ProductResponse testRandomError() {
        ResponseEntity<ResponseDto<ProductResponse>> response = productService.testRandomError();
        if (response.getBody() == null || response.getBody().getData() == null) {
            throw new BusinessRuntimeException("상품 정보를 조회할 수 없습니다.");
        }
        return response.getBody().getData();
    }

    @Retry(name = "productService", fallbackMethod = "getProductFallback")
    @CircuitBreaker(name = "productService", fallbackMethod = "getProductFallback")
    public ProductResponse testTimeout() {
        ResponseEntity<ResponseDto<ProductResponse>> response = productService.testTimeout();
        if (response.getBody() == null || response.getBody().getData() == null) {
            throw new BusinessRuntimeException("상품 정보를 조회할 수 없습니다.");
        }
        return response.getBody().getData();
    }
}
