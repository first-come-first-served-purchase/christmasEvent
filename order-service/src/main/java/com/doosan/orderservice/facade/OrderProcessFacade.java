package com.doosan.orderservice.facade;

import com.doosan.common.dto.order.CreateOrderReqDto;
import com.doosan.orderservice.dto.CreateOrderResDto;
import com.doosan.orderservice.service.ReactiveStockEventService;
import com.doosan.orderservice.service.ReactiveStockService;
import com.doosan.orderservice.service.OrderService;
import com.doosan.orderservice.model.StockEvent;
import com.doosan.orderservice.model.StockEventType;
import com.doosan.common.exception.BusinessRuntimeException;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@Service
@RequiredArgsConstructor
@Log4j2
public class OrderProcessFacade {
    private final ReactiveStockService reactiveStockService;
    private final ReactiveStockEventService reactiveStockEventService;
    private final OrderService orderService;
    
    public Mono<CreateOrderResDto> processOrder(int userId, List<CreateOrderReqDto> orderRequests) {
        return Flux.fromIterable(orderRequests)
            // 재고 확인 및 차감
            .flatMap(request -> 
                reactiveStockService.checkAndReduceStock(
                    request.getProductId(), 
                    request.getQuantity()
                )
                .flatMap(success -> {
                    if (!success) {
                        return Mono.error(new BusinessRuntimeException("재고 부족"));
                    }
                    return Mono.just(request);
                })
            )
            .collectList()
            // 주문 생성
            .flatMap(validatedRequests -> 
                Mono.fromCallable(() -> 
                    orderService.createOrder(userId, validatedRequests)
                )
            )
            // 재고 이벤트 발행
            .flatMap(orderResult -> 
                reactiveStockEventService.publishStockEvent(
                    new StockEvent(
                        StockEventType.STOCK_REDUCED,
                        Long.valueOf(orderResult.getOrderId()),
                        Long.valueOf(orderResult.getTotalPrice())
                    )
                )
                .thenReturn(orderResult)
            )
            .doOnError(error -> 
                log.error("주문 처리 중 오류 발생", error)
            );
    }
} 