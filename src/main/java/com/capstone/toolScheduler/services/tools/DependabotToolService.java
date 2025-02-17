package com.capstone.toolScheduler.services.tools;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class DependabotToolService implements ToolScanService {

    private final WebClient.Builder webClientBuilder;

    public DependabotToolService(WebClient.Builder webClientBuilder) {
        this.webClientBuilder = webClientBuilder;
    }

    @Override
    public String getToolName() {
        return "dependabot";
    }

    @Override
    public String fetchAlerts(String owner, String repository, String token) throws JsonMappingException, JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        List<Map<String, Object>> totalAlerts = new ArrayList<>();

        int page = 1;
        int perPage = 100;

        while(true){
            String url = "https://api.github.com/repos/" + owner + "/" + repository + "/dependabot/alerts?per_page=" + perPage + "&page=" + page;
            String responseData = webClientBuilder.build()
                    .get()
                    .uri(url)
                    .header("Authorization", "Bearer " + token)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

                    List<Map<String, Object>> alerts = objectMapper.readValue(responseData, new TypeReference<List<Map<String, Object>>>() {});

                    if(alerts.isEmpty()) break;
                    totalAlerts.addAll(alerts);
                    if(alerts.size() < perPage) break;
                    page++;
        }

        String finalData = objectMapper.writeValueAsString(totalAlerts);
        return finalData;
    }
}
