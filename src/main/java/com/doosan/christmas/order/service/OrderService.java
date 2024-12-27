package com.doosan.christmas.order.service;

import com.doosan.christmas.member.domain.Member;
import com.doosan.christmas.member.repository.MemberRepository;
import com.doosan.christmas.order.domain.*;
import com.doosan.christmas.order.dto.requestDto.OrderRequestDto;
import com.doosan.christmas.order.dto.responseDto.OrderResponseDto;
import com.doosan.christmas.order.repository.OrderRepository;
import com.doosan.christmas.order.shared.OrderStatus;
import com.doosan.christmas.product.domain.Product;
import com.doosan.christmas.product.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import javax.persistence.EntityNotFoundException;
import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductService productService;
    private final MemberRepository memberRepository;

    // 배송완료 상태로 강제 변경
    @Transactional
    public OrderResponseDto forceCompleteDelivery(Long orderId, Long memberId) {
        Order order = findOrderByIdAndMemberId(orderId, memberId);

        if (order.getStatus() != OrderStatus.DELIVERY_COMPLETED) {
            try {
                // 배송 완료 처리
                order.completeDelivery();

                log.info("배송완료 강제 처리 완료 - orderId: {}", order.getId());

            } catch (Exception e) {
                log.error("배송완료 처리 중 오류 발생 - orderId: {}", order.getId(), e);
                throw e;
            }
        } else {
            log.info("이미 배송완료 상태입니다. - orderId: {}", order.getId());
        }

        return OrderResponseDto.from(order);
    }

    // 주문 상세 조회
    @Transactional(readOnly = true)
    public OrderHistoryResponse getOrderDetail(Long orderId, Long memberId) {
        if (memberId == null) {
            log.error("주문 상세 조회 실패 - memberId가 null입니다.");
            throw new IllegalArgumentException("Member ID는 null일 수 없습니다.");
        }
        log.info("주문 상세 조회 시작 - orderId: {}, memberId: {}", orderId, memberId);
        Order order = findOrderByIdAndMemberId(orderId, memberId);

        log.info("주문 상세 조회 성공 - orderId: {}, memberId: {}, 주문 상태: {}",order.getId(), memberId, order.getStatus());
        // 주문 상세 조회 성공 - orderId: 17, memberId: 19, 주문 상태: ORDER_RECEIVED
        return OrderHistoryResponse.from(order);
    }

    private Order findOrderByIdAndMemberId(Long orderId, Long memberId) {
        return orderRepository.findByIdAndMemberId(orderId, memberId)
                .orElseThrow(() -> new EntityNotFoundException("주문을 찾을 수 없습니다."));
    }

    // 주문 등록
    @Transactional
    public OrderResponseDto createOrder(OrderRequestDto request, Long memberId) {
        log.info("주문 생성 시작 - memberId: {}, request: {}", memberId, request);

        try {

          // 회원을 찾을 수 없는 경우
          Member member = memberRepository.findById(memberId)
                  .orElseThrow(() -> {
                      log.error("회원 조회 실패 - memberId: {}에 대한 회원을 찾을 수 없습니다.", memberId);
                      return new EntityNotFoundException("회원을 찾을 수 없습니다.");
                  });

            // 상품을 찾을 수 없는 경우
            Product product = productService.findProductById(request.getProductId());
            if (product == null) {
                log.error("상품 조회 실패 - productId: {}에 대한 상품을 찾을 수 없습니다.", request.getProductId());
                throw new EntityNotFoundException("상품을 찾을 수 없습니다.");
            }

            // 주문 생성
            Order order = Order.builder()
                    .member(member)
                    .product(product)
                    .quantity(request.getQuantity())
                    .build();
            orderRepository.save(order);

            log.info("주문 생성 완료 - orderId: {}, memberId: {}, productId: {}, quantity: {}",order.getId(), memberId, request.getProductId(), request.getQuantity());
            return OrderResponseDto.from(order);

        } catch (EntityNotFoundException e) {
            log.error("주문 생성 실패 - memberId: {}에서 오류 발생: {}", memberId, e.getMessage());
            throw e;

        } catch (Exception e) {
            log.error("주문 생성 실패 - memberId: {}에서 예상치 못한 오류 발생: {}", memberId, e.getMessage());
            throw new RuntimeException("주문 생성 중 오류가 발생했습니다.", e);  // 예상치 못한 오류는 새로운 예외로 감싸서 던짐
        }
    }

    // 주문 취소
    @Transactional
    public OrderResponseDto cancelOrder(Long orderId, Long memberId) {
        Order order = findOrderByIdAndMemberId(orderId, memberId);
        
        if (!order.canCancel()) {
            throw new IllegalStateException("취소할 수 없는 주문입니다.");
        }
        
        order.updateStatus(OrderStatus.CANCELLED);
        productService.restoreStock(
            order.getOrderSnapshot().getProductId(), 
            order.getOrderSnapshot().getQuantity()
        );
        
        return OrderResponseDto.from(order);
    }

    // 주문 반품
    @Transactional
    public OrderResponseDto returnOrder(Long orderId, Long memberId) {
        try {

            Order order = findOrderByIdAndMemberId(orderId, memberId);

            if (!order.canReturn()) {
                throw new IllegalStateException("반품할 수 없는 주문입니다.");
            }

            order.updateStatus(OrderStatus.RETURN_REQUESTED);
            return OrderResponseDto.from(order);

        } catch (EntityNotFoundException e) {
            // 주문이 없을 경우
            throw new IllegalStateException("해당 주문을 찾을 수 없습니다. 주문 ID: " + orderId);
        }
    }

    // 주문 상태 업데이트 스케줄링 , 매일 자정에 메서드 실행
    @Scheduled(cron = "0 0 0 * * *")
    @Transactional
    public void processOrderStatus() {
        LocalDateTime now = LocalDateTime.now();
        log.info("주문 상태 업데이트 시작 - 현재 시간: {}", now);

        // 주문 접수 -> 배송 중
        log.debug("주문 접수 -> 배송 중 상태 업데이트 시작");
        orderRepository.findByStatus(OrderStatus.ORDER_RECEIVED)
                .stream()
                .filter(order -> order.getCreatedAt().plusDays(1).isBefore(now))
                .forEach(order -> {
                    try {
                        log.debug("주문 상태 업데이트 대상 orderId: {}, createdAt: {}", order.getId(), order.getCreatedAt());

                        order.startDelivery();
                        log.info("배송 시작 처리 완료 - orderId: {}", order.getId());

                    } catch (Exception e) {
                        log.error("주문 상태 업데이트 중 오류 발생 - orderId: {}", order.getId(), e);
                    }
                });

        // 배송 중 -> 배송 완료
        log.debug("배송 중 -> 배송 완료 상태 업데이트 시작");
        orderRepository.findByStatus(OrderStatus.DELIVERED)
                .stream()
                .filter(order -> order.getDeliveryStartDate().plusDays(1).isBefore(now))
                .forEach(order -> {
                    try {
                        log.debug("주문 상태 업데이트 대상 orderId: {}, deliveryStartDate: {}", order.getId(), order.getDeliveryStartDate());

                        order.completeDelivery();
                        log.info("배송 완료 처리 완료 - orderId: {}", order.getId());

                    } catch (Exception e) {
                        log.error("주문 상태 업데이트 중 오류 발생 - orderId: {}", order.getId(), e);
                    }
                });

        // 반품 요청 -> 반품 완료 (createdAt 기준으로 변경)
        log.debug("반품 요청 -> 반품 완료 상태 업데이트 시작");
        orderRepository.findByStatus(OrderStatus.RETURN_REQUESTED)
                .stream()
                .filter(order -> order.getCreatedAt().plusDays(1).isBefore(now))
                .forEach(order -> {
                    try {
                        log.debug("주문 상태 업데이트 대상 orderId: {}, createdAt: {}", order.getId(), order.getCreatedAt());
                        order.updateStatus(OrderStatus.RETURN_COMPLETED);
                        productService.restoreStock(
                                order.getOrderSnapshot().getProductId(),
                                order.getOrderSnapshot().getQuantity()
                        );
                        log.info("반품 완료 처리 및 재고 복구 완료 - orderId: {}", order.getId());

                    } catch (Exception e) {
                        log.error("주문 상태 업데이트 중 오류 발생 - orderId: {}", order.getId(), e);
                    }
                });

        log.info("주문 상태 업데이트 종료 - 현재 시간: {}", LocalDateTime.now());
    }


    // 주문 이력 조회
    @Transactional(readOnly = true)
    public Page<OrderHistoryResponse> getOrderHistory(
            Long memberId, 
            OrderSearchCondition condition,
            Pageable pageable) {
        
        log.debug("주문 이력 조회 시작 - memberId: {}, condition: {}, page: {}", 
                memberId, condition, pageable.getPageNumber());
                
        Page<Order> orderPage = orderRepository.findOrderHistory(
            memberId, 
            condition, 
            pageable
        );
        
        return orderPage.map(OrderHistoryResponse::from);
    }
} 