package com.capstone.toolScheduler.services;

import com.capstone.toolScheduler.dto.event.ParseRequestEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class ParseJobProducerService {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    // Topic name defined in application.yml or fallback to "parser-topic"
    @Value("job_ingestion_topic")
    private String topic;

    public ParseJobProducerService(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendParseJobEvent(ParseRequestEvent event) {
        try {
            // Convert the event to JSON
            String jsonPayload = objectMapper.writeValueAsString(event);
            kafkaTemplate.send(topic, jsonPayload).whenComplete((result, exception) -> {
                if (exception != null) {
                    System.err.println("Failed to send ParseRequestEvent: " + exception.getMessage());
                } else {
                    var metadata = result.getRecordMetadata();
                    System.out.println("ParseRequestEvent sent to topic=" + metadata.topic() +
                            " partition=" + metadata.partition() +
                            " offset=" + metadata.offset());
                }
            });
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }
}
