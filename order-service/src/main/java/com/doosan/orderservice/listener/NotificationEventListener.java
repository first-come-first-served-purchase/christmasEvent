package com.doosan.orderservice.listener;

import com.doosan.orderservice.entity.Notification;
import com.doosan.orderservice.event.NotificationEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.Date;

@Component
@RequiredArgsConstructor
@Log4j2
public class NotificationEventListener {
    private final ReactiveRedisTemplate<String, Notification> reactiveRedisTemplate;

    @KafkaListener(
            topics = "notification-events",
            groupId = "notification-group",
            containerFactory = "notificationKafkaListenerContainerFactory"
    )
    public Mono<Void> handleNotificationEvent(NotificationEvent event) {
        log.info("알림 이벤트 수신: {}", event);

        Notification notification = Notification.builder()
                .userId(event.getUserId()) // 이벤트 사용자 id
                .orderId(event.getOrderId()) // 이벤트 주문 id
                .type(event.getType()) // 이벤트의 알림 타입
                .message(event.getMessage()) // 이벤트 메시지
                .sentAt(new Date()) // 알림 생성 시각
                .build();

        return reactiveRedisTemplate.opsForValue()
                .set(String.format("notification:%d:%d", event.getUserId(), event.getOrderId()), notification)
                .doOnSuccess(v -> log.info("알림 저장 성공 - 타입: {}, 주문: {}", event.getType(), event.getOrderId()))
                .doOnError(e -> log.error("알림 저장 실패", e))
                .then();
    }
}
