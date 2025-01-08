package com.doosan.orderservice.config;

import com.doosan.orderservice.event.OrderEvent;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.*;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;
import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableKafka
public class KafkaConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

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
        return factory;
    }
}
