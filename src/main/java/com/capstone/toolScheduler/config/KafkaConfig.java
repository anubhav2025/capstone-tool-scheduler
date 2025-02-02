package com.capstone.toolScheduler.config;


import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.clients.producer.Producer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.*;
import org.springframework.kafka.support.serializer.JsonDeserializer;

import com.capstone.toolScheduler.dto.ParseJobEvent;
import com.capstone.toolScheduler.dto.ScanEventDTO;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.apache.kafka.common.serialization.StringSerializer;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaConfig {

    /**
     * Create or verify the "scan-topic".
     */
    @Bean
    public NewTopic scanTopic() {
        return TopicBuilder.name("scan-topic")
                .partitions(3)
                .replicas(1)
                .build();
    }

    /**
     * We also create the parser-topic here if not already existing.
     */
    @Bean
    public NewTopic parserTopic() {
        return TopicBuilder.name("parser-topic")
                .partitions(3)
                .replicas(1)
                .build();
    }

    /**
     * ConsumerFactory for ScanEventDTO messages.
     * This configures how to deserialize JSON into our DTO class.
     */
    @Bean
    public ConsumerFactory<String, ScanEventDTO> consumerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "tool-scheduler-group");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);

        // JSON Deserializer config
        JsonDeserializer<ScanEventDTO> jsonDeserializer = new JsonDeserializer<>(ScanEventDTO.class);
        jsonDeserializer.addTrustedPackages("*"); // or restrict to your package
        // If you have turned off type headers in producer, disable them here:
        jsonDeserializer.setUseTypeHeaders(false);

        return new DefaultKafkaConsumerFactory<>(props, new StringDeserializer(), jsonDeserializer);
    }

    /**
     * The container factory that the @KafkaListener will use.
     */
    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, ScanEventDTO> kafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, ScanEventDTO> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory());
        return factory;
    }



     // ------------------------------------------------------------------
    // 1) DEFAULT ProducerFactory - needed by Spring Boot auto-config
    // ------------------------------------------------------------------
    /**
     * This is the **default** bean that Spring Boot's `KafkaAutoConfiguration`
     * requires to create a default `KafkaTemplate<?, ?>`. 
     * 
     * Generics: We use `ProducerFactory<Object, Object>` (or `<?, ?>`) so it 
     * won't conflict with your custom typed factory. 
     */
    @Bean
    public ProducerFactory<Object, Object> producerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        // If you don't want to embed type info:
        // props.put(JsonSerializer.ADD_TYPE_INFO_HEADERS, false);

        return new DefaultKafkaProducerFactory<>(props);
    }

    // ------------------------------------------------------------------
    // 2) CUSTOM ProducerFactory for "ParseJobEvent"
    // ------------------------------------------------------------------
    /**
     * This bean is specifically for writing `ParseJobEvent`s to "parser-topic".
     * It's separate from the default ProducerFactory, 
     * so that you can have typed usage if needed.
     */
    @Bean
    public ProducerFactory<String, ParseJobEvent> parseJobProducerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        // props.put(JsonSerializer.ADD_TYPE_INFO_HEADERS, false);

        return new DefaultKafkaProducerFactory<>(props);
    }

    /**
     * If you want to produce messages manually (no KafkaTemplate):
     * a single Producer bean for ParseJobEvent.
     */
    @Bean
    public Producer<String, ParseJobEvent> parseJobProducer() {
        return parseJobProducerFactory().createProducer();
    }
}