package com.doosan.orderservice.service;

import com.doosan.orderservice.model.StockEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.kafka.sender.KafkaSender;
import reactor.kafka.sender.SenderRecord;

@Service
@RequiredArgsConstructor
@Log4j2
public class ReactiveStockEventService {
    private final KafkaSender<String, StockEvent> kafkaSender;
    private static final String TOPIC = "stock-events";
    
    public Mono<Void> publishStockEvent(StockEvent event) {
        return kafkaSender.send(
            Mono.just(
                SenderRecord.create(
                    new ProducerRecord<>(TOPIC, String.valueOf(event.getProductId()), event),
                    event.getProductId()
                )
            )
        )
        .doOnNext(result -> 
            log.info("재고 이벤트 발행 성공: {}", event)
        )
        .doOnError(error -> 
            log.error("재고 이벤트 발행 실패: {}", event, error)
        )
        .then();
    }
} 