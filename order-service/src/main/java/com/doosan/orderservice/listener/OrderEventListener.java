package com.doosan.orderservice.listener;

import com.doosan.orderservice.event.OrderEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class OrderEventListener {

    @KafkaListener(topics = "${kafka.topic.order-events}")
    public void handleOrderEvent(OrderEvent event) {
        log.info("주문 접수 이벤트: {}", event);

        switch (event.getEventType()) {
            case "PAYMENT_COMPLETED":
                handlePaymentCompleted(event);
                break;
            case "PAYMENT_FAILED":
                handlePaymentFailed(event);
                break;
            case "ORDER_CANCELLED":
                handleOrderCancelled(event);
                break;
        }
    }

    private void handlePaymentCompleted(OrderEvent event) {
        log.info("결제 완료 이벤트 처리됨: {}", event.getOrderId());
    }

    private void handlePaymentFailed(OrderEvent event) {
        log.info("결제 실패 이벤트 처리됨: {}", event.getOrderId());
    }

    private void handleOrderCancelled(OrderEvent event) {
        log.info("주문 취소 이벤트 처리됨: {}", event.getOrderId());
    }
} 