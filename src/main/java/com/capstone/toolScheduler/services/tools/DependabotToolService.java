package com.capstone.toolScheduler.services.tools;

import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

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
    public String fetchAlerts(String owner, String repository, String token) {
        String url = "https://api.github.com/repos/" + owner + "/" + repository + "/dependabot/alerts";
        return webClientBuilder.build()
                .get()
                .uri(url)
                .header("Authorization", "Bearer " + token)
                .retrieve()
                .bodyToMono(String.class)
                .block();
    }
}
