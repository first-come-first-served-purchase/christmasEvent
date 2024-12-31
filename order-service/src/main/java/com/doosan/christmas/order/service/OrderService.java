package com.doosan.christmas.order.service;

import com.doosan.christmas.order.domain.*;
import com.doosan.christmas.order.exception.OrderException;
import com.doosan.christmas.order.repository.OrderRepository;
import com.doosan.christmas.order.shared.OrderStatus;
import com.doosan.christmas.order.dto.requestDto.OrderRequestDto;
import com.doosan.christmas.order.dto.responseDto.OrderResponseDto;
import com.doosan.christmas.order.client.UserServiceClient;
import com.doosan.christmas.order.client.ProductServiceClient;
import com.doosan.christmas.order.client.dto.UserResponse;
import com.doosan.christmas.order.client.dto.ProductResponse;
import feign.FeignException;
import com.doosan.christmas.order.exception.OrderNotFoundException;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

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
        ProductResponse product = productServiceClient.getProductById(order.getProductId());

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

    @Transactional
    public OrderResponseDto createOrder(Long userId, OrderRequestDto request) {
        log.info("[주문 생성 시작] 사용자 ID: {}, 요청 데이터: {}", userId, request);

        try {
            log.debug("[사용자 정보 조회] 사용자 ID: {}", userId);
            UserResponse user = userServiceClient.getUserById(userId);

            log.debug("[상품 정보 조회] 상품 ID: {}", request.getProductId());
            ProductResponse product = productServiceClient.getProductById(request.getProductId());

            log.debug("[주문 데이터 생성 중]");
            Order order = Order.builder()
                    .userId(user.getId())
                    .productId(product.getId())
                    .quantity(request.getQuantity())
                    .totalPrice(product.getPrice().multiply(BigDecimal.valueOf(request.getQuantity())))
                    .build();

            Order savedOrder = orderRepository.save(order);
            log.info("[주문 생성 성공] 주문 ID: {}", savedOrder.getId());
            return OrderResponseDto.from(savedOrder);

        } catch (FeignException e) {
            log.error("[외부 서비스 호출 실패] 메시지: {}", e.getMessage());
            throw new RuntimeException("주문 생성 실패: " + e.getMessage());
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
