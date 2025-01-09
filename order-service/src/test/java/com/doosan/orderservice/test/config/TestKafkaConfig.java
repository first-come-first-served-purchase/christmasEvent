package com.doosan.orderservice.test.config;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.*;
import org.springframework.context.annotation.Profile;
import java.util.HashMap;
import java.util.Map;

@TestConfiguration
@Profile("test")
public class TestKafkaConfig {

    @Bean
    @Primary
    public ConsumerFactory<String, String> testConsumerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092"); // Kafka 서버 주소 설정
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "test-consumer-group"); // 테스트용 consumer ID
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest"); // 가장 처음부터 메시지를 읽도록 설정
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class); // 키 역직렬화
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class); // 값 역직렬화
        return new DefaultKafkaConsumerFactory<>(props); // ConsumerFactory 생성 및 반환
    }

    @Bean
    @Primary
    public ConcurrentKafkaListenerContainerFactory<String, String> testKafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, String> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(testConsumerFactory()); // 테스트용 ConsumerFactory 설정
        return factory;
    }

    @Bean
    @Primary // 기본적으로 사용될 Bean임을 명시
    public KafkaTemplate<String, String> testKafkaTemplate() {
        return new KafkaTemplate<>(testProducerFactory()); // 테스트용 KafkaTemplate 생성 및 반환
    }

    @Bean
    public ProducerFactory<String, String> testProducerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put("bootstrap.servers", "localhost:9092"); // Kafka 서버 주소 설정
        props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer"); // 키 직렬화
        props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer"); // 값 직렬화
        return new DefaultKafkaProducerFactory<>(props);
    }
}