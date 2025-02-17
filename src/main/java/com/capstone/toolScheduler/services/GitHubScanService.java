package com.capstone.toolScheduler.services;


import com.capstone.toolScheduler.dto.ack.ScanRequestAcknowledgement;
import com.capstone.toolScheduler.dto.ack.payload.AcknowledgementEventPayload;
import com.capstone.toolScheduler.dto.event.ParseRequestEvent;
import com.capstone.toolScheduler.dto.event.ScanRequestEvent;
import com.capstone.toolScheduler.dto.event.payload.ParseRequestEventPayload;
import com.capstone.toolScheduler.enums.ToolTypes;
import com.capstone.toolScheduler.model.Tenant;
import com.capstone.toolScheduler.repository.TenantRepository;
import com.capstone.toolScheduler.services.tools.ToolScanService;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.stereotype.Service;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;

@Service
public class GitHubScanService {

    private final TenantRepository tenantRepository;
    private final java.util.List<ToolScanService> toolServices;
    private final ParseJobProducerService parseJobProducerService;
    private final AcknowledgementProducerService acknowledgementProducerService;

    public GitHubScanService(
            TenantRepository tenantRepository,
            java.util.List<ToolScanService> toolServices,
            ParseJobProducerService parseJobProducerService,
            AcknowledgementProducerService acknowledgementProducerService) {
        this.tenantRepository = tenantRepository;
        this.parseJobProducerService = parseJobProducerService;
        this.toolServices = toolServices;
        this.acknowledgementProducerService = acknowledgementProducerService;
    }

    /**
     * Process the ScanRequestEvent:
     * 1. Lookup Tenant by tenantId from the event payload.
     * 2. Determine the tool to scan from the event payload.
     * 3. Fetch alerts from GitHub using the matching ToolScanService.
     * 4. Write alerts JSON to a file.
     * 5. Publish a ParseRequestEvent (with tool, filePath, tenantId, and esIndex).
     */
    public String scanAndStore(ScanRequestEvent event) throws JsonProcessingException {
        // 1. Retrieve the event payload
        var payload = event.getPayload();
        ToolTypes toolType = payload.getTool();
        String tenantId = payload.getTenantId();

        // 2. Look up Tenant details
        Tenant tenant = tenantRepository.findByTenantId(tenantId);
        if (tenant == null) {
            throw new RuntimeException("No tenant found for tenantId=" + tenantId);
        }
        String owner = tenant.getOwner();
        String repo = tenant.getRepo();
        String pat = tenant.getPat();
        String esIndex = tenant.getEsIndex();

        System.out.println("hello");
        System.out.println(toolType.name());

        // 3. Find the matching ToolScanService
        ToolScanService matchedService = toolServices.stream()
                .filter(svc -> svc.getToolName().equalsIgnoreCase(toolType.name()))
                .findFirst()
                .orElse(null);
        StringBuilder resultBuilder = new StringBuilder();
        if (matchedService == null) {
            resultBuilder.append("Skipping unknown tool: ").append(toolType).append("\n");
            return resultBuilder.toString();
        }

        // 4. Fetch alerts from GitHub
        String alertsJson = matchedService.fetchAlerts(owner, repo, pat);

        // 5. Write alerts JSON to a file (using tenantId in path)
        String filePath = writeToFile(toolType, tenantId, alertsJson);

        AcknowledgementEventPayload ackPayload = new AcknowledgementEventPayload(event.getEventId());
        ScanRequestAcknowledgement scanAck = new ScanRequestAcknowledgement(null, ackPayload);

        acknowledgementProducerService.publishAcknowledgement(scanAck);

        // 6. Publish a ParseRequestEvent (includes tool, tenantId, filePath)
        ParseRequestEvent parseEvent = new ParseRequestEvent(
            new ParseRequestEventPayload(toolType, tenantId, filePath)
        );
        parseJobProducerService.sendParseJobEvent(parseEvent);

        resultBuilder.append(toolType)
                     .append(" => ")
                     .append(filePath)
                     .append("\n");
        return resultBuilder.toString();
    }

    /**
     * Writes the fetched JSON to a file, e.g., "Scans/<toolType>/<tenantId>/scan-result.json"
     */
    private String writeToFile(ToolTypes toolType, String tenantId, String content) {
        File dir = new File("Scans" + File.separator
                + toolType.name().toLowerCase()
                + File.separator
                + tenantId);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        File scanFile = new File(dir, "scan-result.json");
        try (Writer writer = new OutputStreamWriter(
                new FileOutputStream(scanFile),
                StandardCharsets.UTF_8
        )) {
            writer.write(content);
        } catch (IOException e) {
            throw new RuntimeException("Failed to write file for tool=" + toolType, e);
        }
        return scanFile.getAbsolutePath();
    }
}
