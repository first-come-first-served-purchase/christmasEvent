package com.doosan.orderservice.listener;

import com.doosan.orderservice.event.OrderEvent;
import com.doosan.orderservice.service.ReactiveStockService;
import com.doosan.orderservice.service.ReactiveNotificationService;
import com.doosan.orderservice.service.ReactivePaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
@Slf4j
@RequiredArgsConstructor
public class OrderEventListener {

    private final ReactiveStockService stockService;
    private final ReactiveNotificationService notificationService;
    private final ReactivePaymentService paymentService;

    @KafkaListener(topics = "${kafka.topic.order-events}")
    public Mono<Void> handleOrderEvent(OrderEvent event) {
        log.info("전달 받은 주문 이벤트: {}", event);

        try {
            switch (event.getEventType()) {
                case "PAYMENT_COMPLETED": // 결제 완료 이벤트 처리
                    return handlePaymentCompleted(event);
                case "PAYMENT_FAILED": // 결제 실패 이벤트 처리
                    return handlePaymentFailed(event);
                case "ORDER_CANCELLED": // 주문 취소 이벤트 처리
                    return handleOrderCancelled(event);
            }
        } catch (Exception e) {
            log.error("이벤트 에러 발생 : {}", event, e);
        }
        return Mono.empty();
    }

    // 결제 완료 이벤트 처리
    private Mono<Void> handlePaymentCompleted(OrderEvent event) {
        return Mono.when(
            // 알림 전송
            notificationService.sendOrderCompletionNotification(
                event.getUserId(), 
                event.getOrderId(),
                event.getTotalAmount()
            ),

            // 결제 처리
            paymentService.processPaymentSettlement(
                event.getOrderId(),
                event.getTotalAmount(),
                event.getUserId(),
                event.getItems()
            ),
            // 재고 차감
            Flux.fromIterable(event.getItems())
                .flatMap(item -> 
                    stockService.confirmStockReduction(
                        item.getProductId(),
                        item.getQuantity()
                    )
                )
        )
        .doOnSuccess(v -> log.info("주문 처리 완료: {}", event.getOrderId()))
        .doOnError(e -> log.error("주문 처리 실패: {}", event, e))
        .onErrorResume(e -> handleCompensation(event));
    }

    // 보상 트랜잭션 처리
    private Mono<Void> handleCompensation(OrderEvent event) {
        return Mono.when(
            // 재고 복구
            Flux.fromIterable(event.getItems())
                .flatMap(item -> stockService.restoreStock(
                    item.getProductId(),
                    item.getQuantity()
                )
                .onErrorResume(e -> {
                    log.error("재고 복구 실패 - productId: {}, quantity: {}, error: {}", 
                        item.getProductId(), 
                        item.getQuantity(), 
                        e.getMessage());
                    return Mono.empty();
                })),
            
            // 결제 취소
            paymentService.cancelPayment(event.getOrderId())
                .onErrorResume(e -> {
                    log.error("결제 취소 실패 - orderId: {}, error: {}", 
                        event.getOrderId(), e.getMessage());
                    return Mono.empty();
                }),
            
            // 실패 알림 전송
            notificationService.sendProcessingFailureNotification(
                event.getUserId(), 
                event.getOrderId()
            )
            .onErrorResume(e -> {
                log.error("알림 전송 실패 - userId: {}, orderId: {}, error: {}", 
                    event.getUserId(), 
                    event.getOrderId(), 
                    e.getMessage());
                return Mono.empty();
            })
        )
        .doOnSuccess(v -> log.info("보상 트랜잭션 완료 - orderId: {}", event.getOrderId()))
        .doOnError(e -> log.error("보상 트랜잭션 실패 - orderId: {}, error: {}", 
            event.getOrderId(), e.getMessage()));
    }

    // 결제 실패 이벤트 처리
    private Mono<Void> handlePaymentFailed(OrderEvent event) {
        return Flux.fromIterable(event.getItems())
                // 재고 복구
            .flatMap(item -> stockService.restoreStock(
                item.getProductId(),
                item.getQuantity()
            ))
                // 결제 실패 기록
            .then(paymentService.recordPaymentFailure(
                event.getOrderId(),
                event.getUserId(),
                event.getPaymentStatus()
            ))
                // 결제 실패 알림 전송
            .then(notificationService.sendPaymentFailureNotification(
                event.getUserId(),
                event.getOrderId()
            ))
            .doOnSuccess(v -> log.info("주문에 대한 결제 실패 이벤트 : {}", event.getOrderId()))
            .doOnError(e -> log.error("주문에 대한 결제 실패 이벤트 처리 실패: {}", event, e))
            .onErrorResume(e -> handleCompensation(event));
    }

    // 주문 취소 이벤트 처리
    private Mono<Void> handleOrderCancelled(OrderEvent event) {
        return paymentService.processRefund(
            event.getOrderId(),
            event.getTotalAmount()
        )
        .then(Mono.when(
                // 재고 복구
            Flux.fromIterable(event.getItems())
                .flatMap(item -> stockService.restoreStock(
                    item.getProductId(),
                    item.getQuantity()
                ))
                .then(),
            // 주문 취소 알림 전송
            notificationService.sendOrderCancellationNotification(
                event.getUserId(),
                event.getOrderId()
            )
        ))
        .doOnSuccess(v -> log.info("주문 취소된 이벤트 : {}", event.getOrderId()))
        .doOnError(e -> log.error("주문 취소된 이벤트 처리 실패: {}", event, e))
        .onErrorResume(e -> handleCompensation(event));
    }
}