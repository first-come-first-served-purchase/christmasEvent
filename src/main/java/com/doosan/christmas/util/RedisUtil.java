package com.doosan.christmas.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;

import java.time.Duration;
@Service
@RequiredArgsConstructor
public class RedisUtil {

    private final StringRedisTemplate redisTemplate;
    private final Logger logger = LoggerFactory.getLogger(RedisUtil.class);

    public void setDataExpire(String key, String value, long duration) {
        redisTemplate.opsForValue().set(key, value, Duration.ofSeconds(duration));
        logger.info("Redis 데이터 저장: key={}, value={}, expire={}초", key, value, duration);
    }

    public void setData(String key, String value) {
        redisTemplate.opsForValue().set(key, value);
        logger.info("Redis 데이터 저장: key={}, value={}", key, value);
    }

    public String getData(String key) {
        String value = redisTemplate.opsForValue().get(key);
        logger.info("Redis 데이터 조회: key={}, value={}", key, value);
        return value;
    }

    public void deleteData(String key) {
        Boolean deleted = redisTemplate.delete(key);
        if (Boolean.TRUE.equals(deleted)) {
            logger.info("Redis 데이터 삭제 성공: key={}", key);
        } else {
            logger.warn("Redis 데이터 삭제 실패 또는 존재하지 않음: key={}", key);
        }
    }
}
