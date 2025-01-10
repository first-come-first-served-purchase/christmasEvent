package com.doosan.orderservice.config;

import com.doosan.orderservice.event.NotificationEvent;
import com.doosan.orderservice.event.OrderEvent;
import com.doosan.orderservice.model.StockEvent;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;
import reactor.kafka.sender.KafkaSender;
import reactor.kafka.sender.SenderOptions;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Configuration
public class KafkaConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;


    //  StockEvent를 전송하기 위한 KafkaSender 생성
    @Bean
    public KafkaSender<String, StockEvent> stockEventKafkaSender() {
        Map<String, Object> props = new HashMap<>();
        props.put("bootstrap.servers", bootstrapServers);
        props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        props.put("value.serializer", "org.springframework.kafka.support.serializer.JsonSerializer");

        SenderOptions<String, StockEvent> senderOptions = SenderOptions.create(props);
        return KafkaSender.create(senderOptions);
    }


    // // OrderEvent를 소비하기 위한 ConsumerFactory 설정
    @Bean
    public ConsumerFactory<String, OrderEvent> consumerFactory() {
        Map<String, Object> config = new HashMap<>();
        config.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers); // Kafka 브로커 주소 설정
        config.put(ConsumerConfig.GROUP_ID_CONFIG, "order-service-group"); // Consumer 그룹 id 설정
        config.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class); // 메시지 키 역직렬화
        config.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class); // 메시지 값 역직렬화
        config.put(JsonDeserializer.TRUSTED_PACKAGES, "*"); // JSON 역직렬화 시, 신뢰 할 수 있는 패키지 설정
        
        return new DefaultKafkaConsumerFactory<>(config, 
            new StringDeserializer(), // 메시지 키 역직렬화
            new JsonDeserializer<>(OrderEvent.class)); // 메시지 값 역직렬화
    }

    // OrderEvent를 처리하기 위한 Kafka Listener Container Factory 설정
    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, OrderEvent> kafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, OrderEvent> factory =
            new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory());
        return factory;
    }

    // NotificationEvent를 전송하기 위한 KafkaSender 생성
    @Bean
    public KafkaSender<String, NotificationEvent> kafkaSender() {
        Map<String, Object> props = new HashMap<>();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers); // Kafka 서버 주소 설정
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class); // 키 직렬화
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class); // 값 직렬화

        //동시에 처리 가능한 최대 메시지 수 설정
        SenderOptions<String, NotificationEvent> senderOptions = SenderOptions.<String, NotificationEvent>create(props)
                .maxInFlight(1024);

        return KafkaSender.create(senderOptions);
    }

    // NotificationEvent를 소비하기 위한 ConsumerFactory 설정
    @Bean
    public ConsumerFactory<String, NotificationEvent> notificationConsumerFactory() {
        Map<String, Object> config = new HashMap<>();
        config.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        config.put(ConsumerConfig.GROUP_ID_CONFIG, "notification-group");
        config.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        config.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        config.put(JsonDeserializer.TRUSTED_PACKAGES, "*");
        config.put(JsonDeserializer.USE_TYPE_INFO_HEADERS, false);
        config.put(JsonDeserializer.VALUE_DEFAULT_TYPE, NotificationEvent.class);

        return new DefaultKafkaConsumerFactory<>(config,
            new StringDeserializer(),
            new JsonDeserializer<>(NotificationEvent.class));
    }

    // NotificationEvent를 처리하기 위한 Kafka Listener Container Factory 설정
    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, NotificationEvent> notificationKafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, NotificationEvent> factory =
            new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(notificationConsumerFactory());
        return factory;
    }
}
