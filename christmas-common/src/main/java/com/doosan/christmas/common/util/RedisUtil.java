package com.doosan.christmas.common.util;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import java.time.Duration;

@Component
@RequiredArgsConstructor
public class RedisUtil {
    
    private final StringRedisTemplate redisTemplate;
    
    public String getData(String key) {
        return redisTemplate.opsForValue().get(key);
    }
    
    public void setData(String key, String value) {
        redisTemplate.opsForValue().set(key, value);
    }
    
    public void setDataExpire(String key, String value, long duration) {
        Duration expireDuration = Duration.ofSeconds(duration);
        redisTemplate.opsForValue().set(key, value, expireDuration);
    }
    
    public void deleteData(String key) {
        redisTemplate.delete(key);
    }
} 