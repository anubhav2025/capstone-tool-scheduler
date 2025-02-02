package com.capstone.toolScheduler.services;

import com.capstone.toolScheduler.dto.ParseJobEvent;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class ParseJobProducerService {

    private final Producer<String, ParseJobEvent> parseJobProducer;

    // Let’s read parser-topic from application.yaml or default to "parser-topic"
    @Value("${parser.kafka.topic:parser-topic}")
    private String parserTopic;

    public ParseJobProducerService(Producer<String, ParseJobEvent> parseJobProducer) {
        this.parseJobProducer = parseJobProducer;
    }

    /**
     * Publish a ParseJobEvent to "parser-topic".
     */
    public void sendParseJobEvent(ParseJobEvent event) {
        ProducerRecord<String, ParseJobEvent> record =
                new ProducerRecord<>(parserTopic, event);

        // You can .get() on the Future if you want synchronous ack:
        parseJobProducer.send(record, (metadata, exception) -> {
            if (exception != null) {
                System.err.println("Failed to send ParseJobEvent: " + exception.getMessage());
            } else {
                System.out.println("ParseJobEvent sent to topic=" + metadata.topic() 
                        + " partition=" + metadata.partition() 
                        + " offset=" + metadata.offset());
            }
        });
    }
}
