package com.doosan.orderservice.service;

import com.doosan.common.exception.BusinessRuntimeException;
import com.doosan.productservice.service.ProductService;
import com.doosan.orderservice.service.StockService;
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
    private final StockService stockService;
    private static final String STOCK_KEY_PREFIX = "product:stock:";

    // 재고 확인 및 차감
    public Mono<Boolean> checkAndReduceStock(Long productId, Long quantity) {
        String stockKey = STOCK_KEY_PREFIX + productId; // Redis 키

        // Redisson 락을 이용해 동시성 제어
        return redissonReactiveClient.getLock("stock:lock:" + productId)
            .tryLock(5, 3, TimeUnit.SECONDS) // 락 대기 시간 5초, 락 유지 시간 3초
            .flatMap(locked -> {
                if (!locked) {
                    return Mono.just(false);
                }

                // 현재 재고를 Redis에서 조회
                return reactiveRedisTemplate.opsForValue()
                    .get(stockKey)
                    .flatMap(currentStock -> {
                        long stock = Long.parseLong(currentStock);

                        // 재고가 충분한 경우
                        if (stock >= quantity) {
                            return reactiveRedisTemplate.opsForValue()
                                .set(stockKey, String.valueOf(stock - quantity))// 재고 차감
                                .doOnSuccess(success -> {

                                    // 재고 모니터링 업데이트 호출
                                    stockService.updateStockMonitoring(productId, stock - quantity, "REDUCE");
                                })
                                .thenReturn(true); // 성공 시 true 반환
                        }
                        return Mono.just(false); // 재고 부족 시 false 반환
                    })
                    .doFinally(signalType -> 
                        redissonReactiveClient.getLock("stock:lock:" + productId).unlock() // 락 해제
                    );
            })
            .doOnError(error -> 
                log.error("재고 확인/차감 중 오류 발생 - 상품: {}, 수량: {}", productId, quantity, error)
            );
    }

//    재고 복구
    public Mono<Void> restoreStock(Long productId, Long quantity) {
        String stockKey = STOCK_KEY_PREFIX + productId;

        // Redis 재고 복구
        return reactiveRedisTemplate.opsForValue()
            .increment(stockKey, quantity)
            .then()
            .doOnError(error -> 
                log.error("재고 복구 중 오류 발생 - 상품: {}, 수량: {}", productId, quantity, error)
            );
    }

    // 재고 차감 확정
    public Mono<Void> confirmStockReduction(Long productId, Long quantity) {
        String stockKey = STOCK_KEY_PREFIX + productId; // Redis 키

        // Redisson 락 이용, 동시성 제어
        return redissonReactiveClient.getLock("stock:lock:" + productId)
            .tryLock(5, 3, TimeUnit.SECONDS)// 락 대기시간 5초, 락 유지시간 3초
            .flatMap(locked -> {
                if (!locked) {
                    return Mono.error(new BusinessRuntimeException("재고 락 획득 실패"));
                }

                // 현재 재고를 Redis에서 조회
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