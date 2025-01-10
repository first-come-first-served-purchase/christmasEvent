package com.doosan.orderservice.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonReactiveClient;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import com.doosan.orderservice.entity.Notification;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import com.doosan.orderservice.entity.PaymentHistory;

@Configuration
@EnableRedisRepositories
public class ReactiveRedisConfig {


    @Value("${spring.data.redis.host}")
    private String redisHost;

    @Value("${spring.data.redis.port}")
    private int redisPort;


    @Primary
    @Bean
    public ReactiveRedisConnectionFactory reactiveRedisConnectionFactory() {
        return new LettuceConnectionFactory(redisHost, redisPort);
    }


    @Bean
    public ReactiveRedisTemplate<String, String> reactiveRedisTemplate(
            ReactiveRedisConnectionFactory connectionFactory) {
        // 문자열 직렬화를 위한 설정
        StringRedisSerializer serializer = new StringRedisSerializer();
        RedisSerializationContext<String, String> serializationContext =
                RedisSerializationContext.<String, String>newSerializationContext()
                        .key(serializer) // 키 직렬화
                        .value(serializer) // 값 직렬화
                        .hashKey(serializer) // 해시 키 직렬화
                        .hashValue(serializer) // 해시 값 직렬화
                        .build();

        return new ReactiveRedisTemplate<>(connectionFactory, serializationContext);
    }

    // Redisson을 위한 Reactive 클라이언트 생성
    @Bean
    public RedissonReactiveClient reactiveRedissonClient() {
        Config config = new Config();
        config.useSingleServer()
                .setAddress("redis://" + redisHost + ":" + redisPort);

        return Redisson.create(config).reactive();
    }

    // 알림 ReactiveRedisTemplate 생성
    @Bean
    public ReactiveRedisTemplate<String, Notification> notificationReactiveRedisTemplate(
            ReactiveRedisConnectionFactory connectionFactory) {

        // 문자열 직렬화를 위한 빌더 생성
        RedisSerializationContext.RedisSerializationContextBuilder<String, Notification> builder =
                RedisSerializationContext.newSerializationContext(new StringRedisSerializer());

        // Notification 객체 직렬화를 위한 설정
        RedisSerializationContext.SerializationPair<Notification> valueSerialization =
                RedisSerializationContext.SerializationPair.fromSerializer(new Jackson2JsonRedisSerializer<>(Notification.class));

        // RedisSerializationContext 생성
        RedisSerializationContext<String, Notification> context = builder
                .value(valueSerialization) // 값 직렬화 설정
                .build();

        // Notification 타입의 ReactiveRedisTemplate 반환
        return new ReactiveRedisTemplate<>(connectionFactory, context);
    }

    // 결제 기록 ReactiveRedisTemplate 생성
    @Bean
    public ReactiveRedisTemplate<String, PaymentHistory> paymentHistoryReactiveRedisTemplate(
            ReactiveRedisConnectionFactory connectionFactory) {

        // 문자열 직렬화를 위한 빌더 생성
        RedisSerializationContext.RedisSerializationContextBuilder<String, PaymentHistory> builder =
                RedisSerializationContext.newSerializationContext(new StringRedisSerializer());

        // PaymentHistory 객체 직렬화를 위한 설정
        RedisSerializationContext.SerializationPair<PaymentHistory> valueSerialization =
                RedisSerializationContext.SerializationPair.fromSerializer(new Jackson2JsonRedisSerializer<>(PaymentHistory.class));

        // RedisSerializationContext 생성
        RedisSerializationContext<String, PaymentHistory> context = builder
                .value(valueSerialization) // 값 직렬화 설정
                .build();

        return new ReactiveRedisTemplate<>(connectionFactory, context);
    }
}
