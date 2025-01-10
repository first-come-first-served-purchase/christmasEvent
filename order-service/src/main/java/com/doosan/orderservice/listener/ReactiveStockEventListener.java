package com.doosan.orderservice.listener;

import com.doosan.orderservice.service.ReactiveStockService;
import com.doosan.orderservice.model.StockEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
@Log4j2
public class ReactiveStockEventListener {
    private final ReactiveStockService reactiveStockService;
    
    @KafkaListener(topics = "stock-events", groupId = "stock-service-group")
    public Mono<Void> handleStockEvent(StockEvent event) {
        log.info("재고 이벤트 수신: {}", event);
        
        return switch (event.getEventType()) {
            case STOCK_REDUCED -> reactiveStockService
                .confirmStockReduction(event.getProductId(), event.getQuantity())
                .doOnSuccess(v -> log.info("재고 차감 확정 완료: {}", event))
                .doOnError(e -> log.error("재고 차감 확정 실패: {}", event, e));
                
            case STOCK_RESTORED -> reactiveStockService
                .restoreStock(event.getProductId(), event.getQuantity())
                .doOnSuccess(v -> log.info("재고 복구 완료: {}", event))
                .doOnError(e -> log.error("재고 복구 실패: {}", event, e));
                
            default -> Mono.error(new IllegalArgumentException("알 수 없는 이벤트 타입: " + event.getEventType()));
        };
    }
} 