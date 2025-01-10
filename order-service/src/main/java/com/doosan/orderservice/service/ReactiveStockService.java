package com.doosan.orderservice.service;

import com.doosan.common.exception.BusinessRuntimeException;
import com.doosan.productservice.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.redisson.api.RedissonReactiveClient;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Log4j2
public class ReactiveStockService {
    private final ReactiveRedisTemplate<String, String> reactiveRedisTemplate;
    private final RedissonReactiveClient redissonReactiveClient;
    private final ProductService productService;
    private static final String STOCK_KEY_PREFIX = "product:stock:";

    public Mono<Boolean> checkAndReduceStock(Long productId, Long quantity) {
        String stockKey = STOCK_KEY_PREFIX + productId;
        
        return redissonReactiveClient.getLock("stock:lock:" + productId)
            .tryLock(5, 3, TimeUnit.SECONDS)
            .flatMap(locked -> {
                if (!locked) {
                    return Mono.just(false);
                }
                
                return reactiveRedisTemplate.opsForValue()
                    .get(stockKey)
                    .flatMap(currentStock -> {
                        long stock = Long.parseLong(currentStock);
                        if (stock >= quantity) {
                            return reactiveRedisTemplate.opsForValue()
                                .set(stockKey, String.valueOf(stock - quantity))
                                .thenReturn(true);
                        }
                        return Mono.just(false);
                    })
                    .doFinally(signalType -> 
                        redissonReactiveClient.getLock("stock:lock:" + productId).unlock()
                    );
            })
            .doOnError(error -> 
                log.error("재고 확인/차감 중 오류 발생 - 상품: {}, 수량: {}", productId, quantity, error)
            );
    }

    public Mono<Void> restoreStock(Long productId, Long quantity) {
        String stockKey = STOCK_KEY_PREFIX + productId;
        
        return reactiveRedisTemplate.opsForValue()
            .increment(stockKey, quantity)
            .then()
            .doOnError(error -> 
                log.error("재고 복구 중 오류 발생 - 상품: {}, 수량: {}", productId, quantity, error)
            );
    }

    public Mono<Void> confirmStockReduction(Long productId, Long quantity) {
        String stockKey = STOCK_KEY_PREFIX + productId;
        
        return redissonReactiveClient.getLock("stock:lock:" + productId)
            .tryLock(5, 3, TimeUnit.SECONDS)
            .flatMap(locked -> {
                if (!locked) {
                    return Mono.error(new BusinessRuntimeException("재고 락 획득 실패"));
                }
                
                return reactiveRedisTemplate.opsForValue()
                    .get(stockKey)
                    .flatMap(currentStock -> {
                        log.info("재고 차감 확정 - 상품: {}, 수량: {}, 현재 재고: {}", 
                            productId, quantity, currentStock);
                        return Mono.empty();
                    })
                    .doFinally(signalType -> 
                        redissonReactiveClient.getLock("stock:lock:" + productId).unlock()
                    );
            })
            .then()
            .doOnError(error -> 
                log.error("재고 차감 확정 중 오류 발생 - 상품: {}, 수량: {}", productId, quantity, error)
            );
    }
} 