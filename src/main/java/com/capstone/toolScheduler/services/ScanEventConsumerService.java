package com.capstone.toolScheduler.services;

import com.capstone.toolScheduler.dto.event.ScanRequestEvent;
import com.capstone.toolScheduler.enums.EventTypes;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

@Service
public class ScanEventConsumerService {

    private final GitHubScanService gitHubScanService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    public ScanEventConsumerService(GitHubScanService gitHubScanService) {
        this.gitHubScanService = gitHubScanService;
    }

    @KafkaListener(
        topics = "jfc_tool",
        containerFactory = "kafkaListenerContainerFactory",
        groupId = "tool-scheduler-group"
    )
    public void consumeScanEvent(@Payload String message) {
        try {
            // System.out.println("[ScanEventConsumerService] Raw message => " + message);

            // 1) Parse top-level to examine "type"
            JsonNode firstRoot = objectMapper.readTree(message);
            JsonNode realRoot = firstRoot;

            // If the entire JSON is double-serialized => unwrap it
            if (firstRoot.isTextual()) {
                String actualJson = firstRoot.asText();
                System.out.println("[ScanEventConsumerService] Double-serialized => " + actualJson);
                realRoot = objectMapper.readTree(actualJson);
            }

            // 2) Check the "type" field
            if (!realRoot.has("type")) {
                System.err.println("[ScanEventConsumerService] Missing 'type' => ignoring.");
                return;
            }

            String typeStr = realRoot.get("type").asText();
            if (!typeStr.equals(EventTypes.SCAN_REQUEST.name())) {
                // e.g. we got "PARSE_REQUEST" or something else
                System.out.println("[ScanEventConsumerService] Not SCAN_REQUEST => ignoring.");
                return;
            }

            // 3) It's SCAN_REQUEST => fully parse as ScanRequestEvent
            ScanRequestEvent event = objectMapper.treeToValue(realRoot, ScanRequestEvent.class);

            // 4) Pass the event to your service
            String summary = gitHubScanService.scanAndStore(event);
            System.out.println("[ScanEventConsumerService] Scan complete:\n" + summary);

        } catch (Exception e) {
            System.err.println("Error processing ScanRequestEvent: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
