package com.doosan.christmas.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
public class RedisUtil {

    private static final Logger logger = LoggerFactory.getLogger(RedisUtil.class);
    private final StringRedisTemplate redisTemplate;

    public RedisUtil(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public String getData(String key) {
        ValueOperations<String, String> operations = redisTemplate.opsForValue();
        String value = operations.get(key);
        logger.info("Redis 데이터 조회: key={}, value={}", key, value);
        return value;
    }

    public void setDataExpire(String key, String value, long seconds) {
        try {
            ValueOperations<String, String> operations = redisTemplate.opsForValue();
            Duration timeout = Duration.ofSeconds(seconds);
            operations.set(key, value, timeout);
            
            // 저장 후 즉시 확인
            String savedValue = operations.get(key);
            logger.info("Redis 데이터 저장 확인: key={}, value={}, savedValue={}", key, value, savedValue);
            
            if (savedValue == null) {
                logger.error("Redis 데이터 저장 실패: key={}, value={}", key, value);
            }
        } catch (Exception e) {
            logger.error("Redis 데이터 저장 중 오류 발생: key={}, value={}, error={}", 
                key, value, e.getMessage(), e);
            throw e;
        }
    }
}
