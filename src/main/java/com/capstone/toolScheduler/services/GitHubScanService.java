package com.capstone.toolScheduler.services;

import org.springframework.stereotype.Service;

import com.capstone.toolScheduler.dto.ParseJobEvent;
import com.capstone.toolScheduler.dto.ScanEventDTO;
import com.capstone.toolScheduler.dto.ScanType;
import com.capstone.toolScheduler.model.Tenant;
import com.capstone.toolScheduler.repository.TenantRepository;
import com.capstone.toolScheduler.services.tools.ToolScanService;
import com.fasterxml.jackson.core.JsonProcessingException;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

@Service
public class GitHubScanService {

    private final TenantRepository tenantRepository;
    private final List<ToolScanService> toolServices;
    private final ParseJobProducerService parseJobProducerService;

    public GitHubScanService(
            TenantRepository tenantRepository,
            List<ToolScanService> toolServices,
            ParseJobProducerService parseJobProducerService) {
        this.tenantRepository = tenantRepository;
        this.parseJobProducerService = parseJobProducerService;
        this.toolServices = toolServices;
    }

    /**
     * 1) Lookup Tenant by tenantId => get (owner, repo, pat, esIndex)
     * 2) For each requested ScanType => fetch data => write to file => produce ParseJobEvent
     */
    public String scanAndStore(ScanEventDTO dto) throws JsonProcessingException {
        // 1) Fetch Tenant
        Tenant tenant = tenantRepository.findByTenantId(dto.getTenantId());
        if (tenant == null) {
            throw new RuntimeException("No tenant found for tenantId=" + dto.getTenantId());
        }

        String owner = tenant.getOwner();
        String repo = tenant.getRepo();
        String pat = tenant.getPat();
        String esIndex = tenant.getEsIndex();

        // 2) Check if the requested list includes ALL
        List<ScanType> requestedTools = dto.getTools();
        if (requestedTools.contains(ScanType.ALL)) {
            requestedTools = Arrays.asList(
                ScanType.DEPENDABOT,
                ScanType.CODESCAN,
                ScanType.SECRETSCAN
            );
        }

        // 3) For each requested tool, call the matching ToolScanService
        StringBuilder resultBuilder = new StringBuilder();
        for (ScanType toolType : requestedTools) {
            // Find the correct service by comparing getToolName() to toolType.name()
            ToolScanService matchedService = toolServices.stream()
                .filter(svc -> svc.getToolName().equalsIgnoreCase(toolType.name()))
                .findFirst()
                .orElse(null);

            if (matchedService == null) {
                resultBuilder.append("Skipping unknown tool: ")
                             .append(toolType)
                             .append("\n");
                continue;
            }

            // 3.1. Fetch alerts from GitHub
            String alertsJson = matchedService.fetchAlerts(owner, repo, pat);

            // 3.2. Write the JSON to file system
            String filePath = writeToFile(toolType, dto.getTenantId(), alertsJson);

            // 3.3. Publish parse event with toolName, filePath, and esIndex
            ParseJobEvent parseEvent = new ParseJobEvent(
                    toolType.name().toLowerCase(),
                    filePath,
                    esIndex
            );
            parseJobProducerService.sendParseJobEvent(parseEvent);

            // Summarize for logs
            resultBuilder.append(toolType)
                         .append(" => ")
                         .append(filePath)
                         .append("\n");
        }

        return resultBuilder.toString();
    }

    /**
     * Writes the fetched JSON to a file:
     * e.g. Scans/<toolType>/<tenantId>/scan-result.json
     */
    private String writeToFile(ScanType toolType, String tenantId, String content) {
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
