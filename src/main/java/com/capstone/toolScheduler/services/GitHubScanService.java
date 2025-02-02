package com.capstone.toolScheduler.services;

import org.springframework.stereotype.Service;

import com.capstone.toolScheduler.dto.ParseJobEvent;
import com.capstone.toolScheduler.dto.ScanEventDTO;
import com.capstone.toolScheduler.dto.ScanType;
import com.capstone.toolScheduler.model.Credential;
import com.capstone.toolScheduler.repository.CredentialRepository;
import com.capstone.toolScheduler.services.tools.ToolScanService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Service
public class GitHubScanService {

    private final CredentialRepository credentialRepository;
    private final List<ToolScanService> toolServices;
    private final ParseJobProducerService parseJobProducerService;

    public GitHubScanService(CredentialRepository credentialRepository,
                             List<ToolScanService> toolServices, ParseJobProducerService parseJobProducerService) {
        this.credentialRepository = credentialRepository;
        this.parseJobProducerService = parseJobProducerService;
        this.toolServices = toolServices;
    }

    public String scanAndStore(ScanEventDTO dto) throws JsonMappingException, JsonProcessingException {
        // 1. Fetch DB credential
        ObjectMapper objectMapper = new ObjectMapper();
        Credential credential = credentialRepository.findByOwnerAndRepository(
                dto.getOwner(), dto.getRepository()
        );
        if (credential == null) {
            throw new RuntimeException("No credentials found for "
                    + dto.getOwner() + "/" + dto.getRepository());
        }

        String pat = credential.getPersonalAccessToken();

        // 2. Check if the requested list includes ALL
        List<ScanType> requestedTools = dto.getTools();
        if (requestedTools.contains(ScanType.ALL)) {
            requestedTools = Arrays.asList(
                ScanType.DEPENDABOT,
                ScanType.CODESCAN,
                ScanType.SECRETSCAN
            );
        }

        // 3. For each requested tool, call the matching ToolScanService
        StringBuilder resultBuilder = new StringBuilder();
        for (ScanType toolType : requestedTools) {
            // Find the matching service by comparing .name() to getToolName()
            // (e.g. "DEPENDABOT" -> "dependabot" or we can uppercase/lowercase as needed)
            ToolScanService matchedService = toolServices.stream()
                    .filter(svc -> svc.getToolName().equalsIgnoreCase(toolType.name()))
                    .findFirst()
                    .orElse(null);

            if (matchedService == null) {
                // Unknown or not implemented
                resultBuilder.append("Skipping unknown tool: ").append(toolType).append("\n");
                continue;
            }

            // 3.1. Fetch alerts
            String alertsJson = matchedService.fetchAlerts(dto.getOwner(),
                    dto.getRepository(), pat);

            List<Map<String, Object>> alerts = objectMapper.readValue(alertsJson, new TypeReference<List<Map<String, Object>>>() {});
            String finalData = objectMapper.writeValueAsString(alerts);

            // 3.2. Write to file
            String filePath = writeToFile(toolType, dto.getOwner(),
                    dto.getRepository(), finalData);
            
            // after saving file in directory save file to 
            ParseJobEvent parseEvent = new ParseJobEvent(toolType.name().toLowerCase(), filePath);
            parseJobProducerService.sendParseJobEvent(parseEvent);
        
            
            resultBuilder.append(toolType).append(" => ").append(filePath).append("\n");
        }

        return resultBuilder.toString();
    }

    private String writeToFile(ScanType toolType, String owner, String repo, String content) {
        File dir = new File("Scans" + File.separator + toolType.name().toLowerCase()
                + File.separator + owner
                + File.separator + repo);

        if (!dir.exists()) {
            dir.mkdirs();
        }

        File scanFile = new File(dir, "scan-result.json");
        try (Writer writer = new OutputStreamWriter(
            new FileOutputStream(scanFile), StandardCharsets.UTF_8)) {
            writer.write(content);
        } catch (IOException e) {
            throw new RuntimeException("Failed to write file for tool=" + toolType, e);
        }
        return scanFile.getAbsolutePath();
    }

}