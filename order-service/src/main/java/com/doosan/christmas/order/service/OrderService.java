package com.doosan.christmas.order.service;

import com.doosan.christmas.order.client.ProductResponseWrapper;
import com.doosan.christmas.order.client.ProductServiceClient;
import com.doosan.christmas.order.client.UserServiceClient;
import com.doosan.christmas.order.client.dto.ProductResponse;
import com.doosan.christmas.order.client.dto.UserResponse;
import com.doosan.christmas.order.domain.Order;
import com.doosan.christmas.order.domain.OrderHistoryResponse;
import com.doosan.christmas.order.domain.Product;
import com.doosan.christmas.order.dto.requestDto.OrderRequestDto;
import com.doosan.christmas.order.dto.responseDto.OrderResponseDto;
import com.doosan.christmas.order.exception.OrderException;
import com.doosan.christmas.order.exception.OrderNotFoundException;
import com.doosan.christmas.order.repository.OrderRepository;
import com.doosan.christmas.order.shared.OrderStatus;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class OrderService {

    private final OrderRepository orderRepository;
    private final UserServiceClient userServiceClient;
    private final ProductServiceClient productServiceClient;

    private Order findOrderByIdAndUserId(Long orderId, Long userId) {
        log.debug("[주문 조회] 주문 ID: {}, 사용자 ID: {}", orderId, userId);
        return orderRepository.findByIdAndUserId(orderId, userId)
                .orElseThrow(() -> {
                    log.error("[주문 조회 실패] 해당 주문을 찾을 수 없음 - 주문 ID: {}, 사용자 ID: {}", orderId, userId);
                    return new OrderNotFoundException("주문을 찾을 수 없습니다.");
                });
    }

    @Transactional
    public OrderResponseDto forceCompleteDelivery(Long orderId, Long userId) {
        log.info("[배송완료 강제 처리 시작] 주문 ID: {}, 사용자 ID: {}", orderId, userId);
        Order order = findOrderByIdAndUserId(orderId, userId);

        if (order.getStatus() != OrderStatus.DELIVERY_COMPLETED) {
            try {
                order.completeDelivery();
                log.info("[배송완료 처리 성공] 주문 ID: {}", order.getId());
            } catch (Exception e) {
                log.error("[배송완료 처리 실패] 주문 ID: {}", orderId, e);
                throw new OrderException.InvalidOrderStatusException("배송완료 처리 중 오류가 발생했습니다.");
            }
        } else {
            log.info("[배송완료 상태 확인] 이미 배송완료 상태입니다 - 주문 ID: {}", orderId);
        }

        return OrderResponseDto.from(order);
    }

    @Transactional(readOnly = true)
    public OrderHistoryResponse getOrderDetail(Long orderId, Long userId) {
        log.info("[주문 상세 조회 시작] 주문 ID: {}, 사용자 ID: {}", orderId, userId);
        Order order = findOrderByIdAndUserId(orderId, userId);
        ProductResponse product = productServiceClient.getProductById(order.getProductId()).getData();

        log.info("[주문 상세 조회 성공] 주문 ID: {}, 상품 이름: {}", orderId, product.getName());
        return OrderHistoryResponse.builder()
                .orderId(order.getId())
                .userId(order.getUserId())
                .productId(order.getProductId())
                .productName(product.getName())
                .description(product.getDescription())
                .quantity(order.getQuantity())
                .price(order.getTotalPrice())
                .status(order.getStatus())
                .orderDate(order.getCreatedAt())
                .deliveryStartDate(order.getDeliveryStartDate())
                .deliveryCompleteDate(order.getDeliveryCompleteDate())
                .build();
    }

    private Order createOrder(Long userId, OrderRequestDto request, ProductResponse product) {
        log.debug("[################### 주문 생성 시작 ###################]");
        log.debug("[요청 데이터] 사용자 ID: {}, 요청 데이터: {}", userId, request);
        log.debug("[상품 데이터] 상품 ID: {}, 상품 이름: {}, 상품 가격: {}, 상품 재고: {}",
                product.getId(), product.getName(), product.getPrice(), product.getStock());

        // 가격 정보 검증
        if (product.getPrice() == null) {
            log.error("[주문 생성 실패] 상품 ID: {}, 이유: 가격 정보가 없습니다", product.getId());
            throw new OrderException("상품 가격 정보가 없습니다.");
        }

        BigDecimal quantity = BigDecimal.valueOf(request.getQuantity());
        BigDecimal totalPrice = quantity.multiply(product.getPrice()); // 가격과 수량 계산

        log.debug("[주문 데이터 계산] 수량: {}, 총 가격: {}", request.getQuantity(), totalPrice);

        Order order = Order.builder()
                .userId(userId)
                .productId(product.getId())
                .quantity(request.getQuantity())
                .totalPrice(totalPrice)
                .build();

        log.debug("[주문 엔터티 빌드 완료] 엔터티 데이터: {}", order);

        try {

            Order savedOrder = orderRepository.save(order);
            log.info("[주문 저장 완료] 주문 ID: {}, 사용자 ID: {}, 상품 ID: {}, 총 가격: {}",
                    savedOrder.getId(), userId, product.getId(), totalPrice);
            return savedOrder;
        } catch (Exception e) {
            log.error("[주문 저장 실패] 사용자 ID: {}, 상품 ID: {}, 에러 메시지: {}",
                    userId, product.getId(), e.getMessage(), e);
            throw new OrderException("주문 저장 중 오류가 발생했습니다.");
        }
    }


    private ProductResponse getProduct(Long productId) {
        log.debug("[상품 정보 조회] 상품 ID: {}", productId);
        ProductResponseWrapper responseWrapper = productServiceClient.getProductById(productId);

        if (!responseWrapper.isSuccess() || responseWrapper.getData() == null) {
            log.error("[상품 정보 조회 실패] 상품 ID: {}", productId);
            throw new OrderException("상품 정보를 조회할 수 없습니다.");
        }

        return responseWrapper.getData(); // ProductResponse 반환
    }


    @Transactional
    public List<OrderResponseDto> createOrders(Long userId, List<OrderRequestDto> orderRequests) {
        log.info("[주문 생성 시작] 사용자 ID: {}, 요청 데이터 수: {}", userId, orderRequests.size());

        try {
            log.debug("[사용자 정보 조회] 사용자 ID: {}", userId);
            ResponseEntity<UserResponse> response = userServiceClient.getUserById(userId);

            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                throw new OrderException("사용자를 찾을 수 없습니다.");
            }

            List<OrderResponseDto> responses = new ArrayList<>();
            for (OrderRequestDto request : orderRequests) {
                log.debug("[상품 정보 조회] 상품 ID: {}", request.getProductId());

                try {
                    ProductResponse product = getProduct(request.getProductId());
                    Order order = createOrder(userId, request, product);
                    responses.add(OrderResponseDto.from(order));
                } catch (Exception e) {
                    log.error("[상품 정보 오류] 상품 ID: {}", request.getProductId(), e);
                    throw new OrderException("상품 정보를 조회할 수 없습니다.");
                }
            }

            log.info("[주문 생성 완료] 사용자 ID: {}, 생성된 주문 수: {}", userId, responses.size());
            return responses;
        } catch (FeignException e) {
            log.error("[Feign 클라이언트 오류] 사용자 ID: {}", userId, e);
            throw new OrderException("서비스 호출 중 오류가 발생했습니다.");
        } catch (OrderException e) {
            throw e;
        } catch (Exception e) {
            log.error("[주문 생성 실패] 사용자 ID: {}", userId, e);
            throw new OrderException("주문 생성 중 오류가 발생했습니다.");
        }
    }


    @Transactional
    public void cancelOrder(Long orderId, Long userId) {
        log.info("[주문 취소 시작] 주문 ID: {}, 사용자 ID: {}", orderId, userId);

        try {
            Order order = findOrderByIdAndUserId(orderId, userId);

            if (!order.canCancel()) {
                log.warn("[주문 취소 불가] 현재 상태에서 취소할 수 없음 - 주문 ID: {}", orderId);
                throw new OrderException.InvalidOrderStatusException("취소할 수 없는 주문 상태입니다.");
            }

            order.updateStatus(OrderStatus.CANCELLED);
            log.info("[주문 상태 업데이트] 주문 취소 완료 - 주문 ID: {}", orderId);

            productServiceClient.restoreStock(order.getProductId(), order.getQuantity());
            log.info("[재고 복구 완료] 상품 ID: {}, 복구된 수량: {}", order.getProductId(), order.getQuantity());

        } catch (Exception e) {
            log.error("[주문 취소 중 오류 발생] 주문 ID: {}", orderId, e);
            throw new OrderException.OrderCancellationException("주문 취소 중 오류가 발생했습니다.");
        }
    }

    @Scheduled(cron = "0 0 0 * * *")
    @Transactional
    public void processOrderStatus() {
        LocalDateTime now = LocalDateTime.now();
        log.info("[주문 상태 업데이트 시작] 현재 시간: {}", now);

        orderRepository.findByStatus(OrderStatus.ORDER_RECEIVED)
                .stream()
                .filter(order -> order.getCreatedAt().plusDays(1).isBefore(now))
                .forEach(order -> {
                    try {
                        order.startDelivery();
                        log.info("[배송 시작 처리 완료] 주문 ID: {}", order.getId());
                    } catch (Exception e) {
                        log.error("[배송 시작 처리 중 오류 발생] 주문 ID: {}", order.getId(), e);
                    }
                });

        orderRepository.findByStatus(OrderStatus.DELIVERED)
                .stream()
                .filter(order -> order.getDeliveryStartDate().plusDays(1).isBefore(now))
                .forEach(order -> {
                    try {
                        order.completeDelivery();
                        log.info("[배송 완료 처리 완료] 주문 ID: {}", order.getId());
                    } catch (Exception e) {
                        log.error("[배송 완료 처리 중 오류 발생] 주문 ID: {}", order.getId(), e);
                    }
                });

        log.info("[주문 상태 업데이트 종료] 현재 시간: {}", LocalDateTime.now());
    }
}
