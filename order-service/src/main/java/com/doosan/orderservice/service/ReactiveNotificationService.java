package com.doosan.orderservice.service;

import com.doosan.orderservice.entity.Notification;
import com.doosan.orderservice.event.NotificationEvent;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import reactor.core.publisher.Mono;
import reactor.kafka.sender.KafkaSender;
import reactor.kafka.sender.SenderRecord;
import org.apache.kafka.clients.producer.ProducerRecord;

@Service
@RequiredArgsConstructor
@Log4j2
public class ReactiveNotificationService {
    private final ReactiveRedisTemplate<String, Notification> reactiveRedisTemplate;
    private final KafkaSender<String, NotificationEvent> kafkaSender;
    private static final String TOPIC = "notification-events";

    // 주문 완료 알림 전송
    public Mono<Void> sendOrderCompletionNotification(Long userId, Long orderId, Long totalAmount) {
        NotificationEvent event = NotificationEvent.builder()
                .type("ORDER_COMPLETION")
                .userId(userId)
                .orderId(orderId)
                .amount(totalAmount)
                .message(String.format("주문이 완료되었습니다. 주문번호: %d, 금액: %d원", orderId, totalAmount))
                .build();

        return sendNotificationEvent(event);
    }

    // 주문 처리 오류 알림 전송
    public Mono<Void> sendProcessingFailureNotification(Long userId, Long orderId) {
        NotificationEvent event = NotificationEvent.builder()
                .type("PROCESSING_FAILURE")
                .userId(userId)
                .orderId(orderId)
                .message(String.format("주문 처리 중 오류가 발생했습니다. 주문번호: %d", orderId))
                .build();

        return sendNotificationEvent(event);
    }

    // 결제 실패 알림 전송
    public Mono<Void> sendPaymentFailureNotification(Long userId, Long orderId) {
        NotificationEvent event = NotificationEvent.builder()
                .type("PAYMENT_FAILURE")
                .userId(userId)
                .orderId(orderId)
                .message(String.format("결제가 실패했습니다. 주문번호: %d", orderId))
                .build();

        return sendNotificationEvent(event);
    }

    // 주문 취소 알림 전송
    public Mono<Void> sendOrderCancellationNotification(Long userId, Long orderId) {
        NotificationEvent event = NotificationEvent.builder()
                .type("ORDER_CANCELLATION")
                .userId(userId)
                .orderId(orderId)
                .message(String.format("주문이 취소되었습니다. 주문번호: %d", orderId))
                .build();

        return sendNotificationEvent(event);
    }

    // 알림 전송 이벤트 발행
    private Mono<Void> sendNotificationEvent(NotificationEvent event) {
        return kafkaSender.send(
            Mono.just(
                SenderRecord.create(
                    new ProducerRecord<>(TOPIC, event.getOrderId().toString(), event),
                    event.getOrderId()
                )
            )
        )
        .doOnNext(result -> log.info("알림 이벤트 발행 성공 - 타입: {}, 주문: {}", 
                event.getType(), event.getOrderId()))
        .doOnError(e -> log.error("알림 이벤트 발행 실패", e))
        .then();
    }
} 