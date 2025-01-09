package com.doosan.orderservice.service;

import com.doosan.orderservice.event.OrderEvent;
import com.doosan.orderservice.event.OrderItemEvent;
import com.doosan.orderservice.entity.Order;
import com.doosan.orderservice.entity.OrderItem;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderEventService {

    private final KafkaTemplate<String, OrderEvent> kafkaTemplate;

    @Value("${kafka.topic.order-events}")
    private String orderTopic;

    // 결제 완료 이벤트
    public void publishPaymentCompletedEvent(Order order, List<OrderItem> items) {
        publishOrderEvent(order, items, "PAYMENT_COMPLETED"); // PAYMENT_COMPLETED 이벤트 타입으로 주문 이벤트 전송
    }

    // 결제 실패 이벤트
    public void publishPaymentFailedEvent(Order order, List<OrderItem> items) {
        publishOrderEvent(order, items, "PAYMENT_FAILED"); // PAYMENT_FAILED 이벤트 타입으로 주문 이벤트 전송
    }

    // 주문 취소 이벤트
    public void publishOrderCancelledEvent(Order order, List<OrderItem> items) {
        publishOrderEvent(order, items, "ORDER_CANCELLED"); // ORDER_CANCELLED 이벤트 타입으로 주문 이벤트 전송
    }

    // 주문 이벤트 공통
    private void publishOrderEvent(Order order, List<OrderItem> orderItems, String eventType) {
        // OrderEvent 객체 생성
        OrderEvent event = OrderEvent.builder()
                .eventType(eventType) // 이벤트 타입 (예: PAYMENT_COMPLETED)
                .orderId((long) order.getId()) // 주문 ID
                .userId((long) order.getUserId()) // 사용자 ID
                .totalAmount((long) order.getTotalPrice()) // 총 금액
                .paymentStatus(order.getPaymentStatus()) // 결제 상태
                .eventDate(new Date()) // 이벤트 발생 시간
                .items(orderItems.stream() // 주문 항목 리스트를 OrderItemEvent 객체로 변환
                        .map(this::convertToOrderItemEvent)
                        .collect(Collectors.toList()))
                .build();

        try {
            // Kafka로 이벤트 전송
            kafkaTemplate.send(orderTopic, String.valueOf(order.getId()), event)
                    .whenComplete((result, ex) -> {
                        // 메시지 전송 완료 후 콜백
                        if (ex == null) {
                            log.info("주문 이벤트가 성공적으로 게시되었습니다. 주문 ID: {}, 이벤트 타입: {}",
                                    order.getId(), eventType); // 성공적인 게시 로그
                        } else {
                            log.error("주문 이벤트 게시에 실패했습니다. 주문 ID: {}, 이벤트 타입: {}",
                                    order.getId(), eventType, ex); // 실패한 게시 로그
                        }
                    });
        } catch (Exception e) {
            // 예외 발생 시 로그 기록
            log.error("주문 이벤트 게시 중 오류가 발생했습니다. 주문 ID : {}, 이벤트 타입: {}",
                    order.getId(), eventType, e);
        }
    }

    // OrderItem을 OrderItemEvent로 변환하는 메서드
    private OrderItemEvent convertToOrderItemEvent(OrderItem item) {
        return OrderItemEvent.builder()
                .productId((long) item.getProductId()) // 상품 ID
                .quantity((long) item.getQuantity()) // 수량
                .price((long) item.getPrice()) // 가격
                .build();
    }
}
