package com.doosan.christmas.common.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
@Configuration
@EnableCaching // 캐시 활성화 설정
public class RedisConfig {

    @Value("${spring.redis.host}") // Redis 호스트 설정
    private String redisHost;

    @Value("${spring.redis.port}") // Redis 포트 설정
    private int redisPort;

    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
        // Redis 연결 생성
        return new LettuceConnectionFactory(redisHost, redisPort);
    }

    @Bean
    public CacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        // Redis 캐시 설정 (TTL: 30분, JSON 직렬화)
        RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(30)) // 캐시 유효 기간
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer())) // 키 직렬화
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(new GenericJackson2JsonRedisSerializer())); // 값 직렬화

        // 캐시 매니저 생성
        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(config)
                .build();
    }
}
