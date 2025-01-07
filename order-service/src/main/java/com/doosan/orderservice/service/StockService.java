package com.doosan.orderservice.service;

import com.doosan.common.dto.ResponseDto;
import com.doosan.productservice.dto.ProductResponse;
import com.doosan.productservice.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;
import java.util.Date;
import java.util.List;
import java.util.ArrayList;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Log4j2
public class StockService {
    private final RedisTemplate<String, Object> redisTemplate;
    private final RedissonClient redissonClient;
    private final ProductService productService;
    private static final String STOCK_KEY_PREFIX = "product:stock:";
    private static final String STOCK_MONITOR_KEY_PREFIX = "product:monitor:";

    // 재고 초기화 (상품 서비스의 재고를 Redis에 동기화)
    public void initializeStock(Long productId) {
        String lockKey = "lock:" + STOCK_KEY_PREFIX + productId;
        RLock lock = redissonClient.getLock(lockKey);

        try {
            if (lock.tryLock(5, 3, TimeUnit.SECONDS)) {
                ResponseEntity<ResponseDto<ProductResponse>> response =
                    productService.getProduct(productId);
                if (response.getBody() != null && response.getBody().getData() != null) {
                    Long stock = Long.valueOf(response.getBody().getData().getQuantity());
                    redisTemplate.opsForValue().set(
                        STOCK_KEY_PREFIX + productId, 
                        stock
                    );
                    log.info("재고 초기화 완료. 상품 ID: {}, 수량: {}", productId, stock);
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("재고 초기화 중 오류 발생", e);
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    // 재고 복구 (주문 취소/실패 시)
    public void restoreStock(Long productId, Long quantity) {
        String lockKey = "lock:" + STOCK_KEY_PREFIX + productId;
        RLock lock = redissonClient.getLock(lockKey);

        try {
            if (lock.tryLock(5, 3, TimeUnit.SECONDS)) {
                Object stockObj = redisTemplate.opsForValue().get(STOCK_KEY_PREFIX + productId);
                Long currentStock = null;
                
                if (stockObj instanceof Integer) {
                    currentStock = Long.valueOf((Integer) stockObj);
                } else if (stockObj instanceof Long) {
                    currentStock = (Long) stockObj;
                }
                
                if (currentStock != null) {
                    Long newStock = currentStock + quantity;
                    redisTemplate.opsForValue().set(STOCK_KEY_PREFIX + productId, newStock);
                    updateStockMonitoring(productId, newStock, "INCREASE");
                    
                    log.info("재고 복구 완료. 상품 ID: {}, 현재 재고: {}, 복구 수량: {}, 최종 재고: {}", 
                        productId, currentStock, quantity, newStock);
                } else {
                    initializeStock(productId);
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("재고 복구 중 오류 발생", e);
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    // 재고 모니터링 정보 업데이트
    private void updateStockMonitoring(Long productId, Long currentStock, String operation) {
        String monitorKey = STOCK_MONITOR_KEY_PREFIX + productId;
        String monitorInfo = String.format(
            "{\"timestamp\":\"%s\",\"stock\":%d,\"operation\":\"%s\"}", 
            new Date(), currentStock, operation
        );
        redisTemplate.opsForList().leftPush(monitorKey, monitorInfo);
        redisTemplate.expire(monitorKey, 24, TimeUnit.HOURS);
    }

    // 재고 모니터링 정보 조회
    public List<String> getStockMonitoringHistory(Long productId) {
        String monitorKey = STOCK_MONITOR_KEY_PREFIX + productId;
        Long size = redisTemplate.opsForList().size(monitorKey);
        if (size == null || size == 0) {
            return new ArrayList<>();
        }
        
        List<Object> rawList = redisTemplate.opsForList().range(monitorKey, 0, size - 1);
        if (rawList == null) {
            return new ArrayList<>();
        }
        
        return rawList.stream()
                .map(Object::toString)
                .collect(Collectors.toList());
    }

    public boolean tryAcquireStock(Long productId, Long quantity) {
        String lockKey = "lock:" + STOCK_KEY_PREFIX + productId;
        RLock lock = redissonClient.getLock(lockKey);

        try {
            boolean isLocked = lock.tryLock(5, 3, TimeUnit.SECONDS);
            if (!isLocked) {
                log.warn("재고 락 획득 실패. 상품 ID: {}", productId);
                return false;
            }

            Object stockObj = redisTemplate.opsForValue().get(STOCK_KEY_PREFIX + productId);
            Long currentStock = null;
            
            if (stockObj instanceof Integer) {
                currentStock = Long.valueOf((Integer) stockObj);
            } else if (stockObj instanceof Long) {
                currentStock = (Long) stockObj;
            }

            if (currentStock == null) {
                initializeStock(productId);
                stockObj = redisTemplate.opsForValue().get(STOCK_KEY_PREFIX + productId);
                if (stockObj instanceof Integer) {
                    currentStock = Long.valueOf((Integer) stockObj);
                } else if (stockObj instanceof Long) {
                    currentStock = (Long) stockObj;
                }
            }

            if (currentStock == null || currentStock < quantity) {
                log.warn("재고 부족. 상품 ID: {}, 현재 재고: {}, 요청 수량: {}", 
                    productId, currentStock, quantity);
                return false;
            }

            redisTemplate.opsForValue().set(STOCK_KEY_PREFIX + productId, currentStock - quantity);
            updateStockMonitoring(productId, currentStock - quantity, "DECREASE");
            
            log.info("재고 차감 성공. 상품 ID: {}, 차감 수량: {}, 남은 재고: {}", 
                productId, quantity, currentStock - quantity);
            return true;

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("재고 차감 중 인터럽트 발생. 상품 ID: {}", productId, e);
            return false;
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }
}
