package com.doosan.orderservice.config;

import com.doosan.orderservice.event.OrderEvent;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.*;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.springframework.util.backoff.FixedBackOff;
import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableKafka
@Slf4j
public class KafkaConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Value("${kafka.topic.order-events-dlt}")
    private String orderDltTopic;

    // ProducerFactory 설정
    @Bean
    public ProducerFactory<String, OrderEvent> orderEventProducerFactory() {
        // Kafka Producer 설정 정보
        Map<String, Object> config = new HashMap<>();
        config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers); // Kafka 서버 주소
        config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class); // Key 직렬화 방식
        config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class); // Value 직렬화 방식
        return new DefaultKafkaProducerFactory<>(config); // ProducerFactory 생성
    }

    //KafkaTemplate 설정
    @Bean
    public KafkaTemplate<String, OrderEvent> orderEventKafkaTemplate() {
        return new KafkaTemplate<>(orderEventProducerFactory());
    }

    // ConsumerFactory 설정
    @Bean
    public ConsumerFactory<String, OrderEvent> orderEventConsumerFactory() {
        // Kafka Consumer 설정
        Map<String, Object> config = new HashMap<>();
        config.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers); // Kafka 서버 주소
        config.put(ConsumerConfig.GROUP_ID_CONFIG, "order-service-group"); // Consumer 그룹 ID
        config.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class); // Key 역직렬화 방식
        config.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class); // Value 역직렬화 방식
        config.put(ConsumerConfig.MAX_POLL_INTERVAL_MS_CONFIG, "300000"); // 5분
        config.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false");
        return new DefaultKafkaConsumerFactory<>(config,
                new StringDeserializer(), // Key 역직렬화
                new JsonDeserializer<>(OrderEvent.class)); // Value 역직렬화 (OrderEvent 객체)
    }

    // Kafka 리스너 설정
    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, OrderEvent> kafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, OrderEvent> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(orderEventConsumerFactory()); // ConsumerFactory 설정
        
        // DLT 설정
        DeadLetterPublishingRecoverer recoverer = new DeadLetterPublishingRecoverer(
            orderEventKafkaTemplate(),
            (record, ex) -> new TopicPartition(orderDltTopic, 0)
        );

        // 에러 핸들러 설정 (3번 재시도, 1초 간격)
        DefaultErrorHandler errorHandler = new DefaultErrorHandler(
            recoverer, 
            new FixedBackOff(1000L, 3L)
        );

        errorHandler.addNotRetryableExceptions(IllegalArgumentException.class);
        factory.setCommonErrorHandler(errorHandler);
        return factory;
    }
}
