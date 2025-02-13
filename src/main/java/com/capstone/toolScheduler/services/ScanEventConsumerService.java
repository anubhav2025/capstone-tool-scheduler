package com.capstone.toolScheduler.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import com.capstone.toolScheduler.dto.ScanEventDTO;
import com.fasterxml.jackson.core.JsonProcessingException;

/**
 * Consumes messages from "scan-topic" and calls GitHubScanService.
 */
@Service
public class ScanEventConsumerService {

    private final GitHubScanService gitHubScanService;

    @Autowired
    public ScanEventConsumerService(GitHubScanService gitHubScanService) {
        this.gitHubScanService = gitHubScanService;
    }

    @KafkaListener(
        topics = "scan-topic",
        groupId = "tool-scheduler-group",
        containerFactory = "kafkaListenerContainerFactory"
    )
    public void consumeScanEvent(@Payload ScanEventDTO eventDTO) throws JsonProcessingException {
        // The new eventDTO has tenantId + tools
        String summary = gitHubScanService.scanAndStore(eventDTO);
        System.out.println("Scan complete:\n" + summary);
    }
}
