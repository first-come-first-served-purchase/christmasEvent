package com.doosan.orderservice.listener;

import com.doosan.orderservice.event.OrderEvent;
import com.doosan.orderservice.service.StockService;
import com.doosan.orderservice.service.NotificationService;
import com.doosan.orderservice.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class OrderEventListener {

    private final StockService stockService;
    private final NotificationService notificationService;
    private final PaymentService paymentService;

    @KafkaListener(topics = "${kafka.topic.order-events}")
    public void handleOrderEvent(OrderEvent event) {
        log.info("전달 받은 주문 이벤트: {}", event);

        try {
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
        } catch (Exception e) {
            log.error("이벤트 에러 발생 : {}", event, e);
        }
    }

    private void handlePaymentCompleted(OrderEvent event) {
        // 알림 서비스 호출
        notificationService.sendOrderCompletionNotification(
            event.getUserId(), 
            event.getOrderId(),
            event.getTotalAmount()
        );

        // 정산 서비스에 결제 정보 전달
        paymentService.processPaymentSettlement(
            event.getOrderId(),
            event.getTotalAmount(),
            event.getUserId(),
            event.getItems()
        );

        // 재고 서비스에 재고 차감 확정
        event.getItems().forEach(item -> 
            stockService.confirmStockReduction(
                item.getProductId(),
                item.getQuantity()
            )
        );

        log.info("주문에 대한 결제 완료 이벤트 : {}",event.getOrderId());
    }

    private void handlePaymentFailed(OrderEvent event) {
        // 재고 복구
        event.getItems().forEach(item -> 
            stockService.restoreStock(
                item.getProductId(),
                item.getQuantity()
            )
        );

        // 실패 통계 수집
        paymentService.recordPaymentFailure(
            event.getOrderId(),
            event.getUserId(),
            event.getPaymentStatus()
        );

        // 사용자에게 실패알림
        notificationService.sendPaymentFailureNotification(
            event.getUserId(),
            event.getOrderId()
        );

        log.info("주문에 대한 결제 실패 이벤트 : {}",event.getOrderId());
    }

    private void handleOrderCancelled(OrderEvent event) {
        // 환불 처리
        paymentService.processRefund(
            event.getOrderId(),
            event.getTotalAmount()
        );

        // 재고 복구
        event.getItems().forEach(item -> 
            stockService.restoreStock(
                item.getProductId(),
                item.getQuantity()
            )
        );

        // 취소 완료 알림
        notificationService.sendOrderCancellationNotification(
            event.getUserId(),
            event.getOrderId()
        );

        log.info("주문 취소된 이벤트 : {}",event.getOrderId());
    }
}