package com.capstone.toolScheduler.services.tools;

import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
public class CodeScanToolService implements ToolScanService {

    private final WebClient.Builder webClientBuilder;

    public CodeScanToolService(WebClient.Builder webClientBuilder) {
        this.webClientBuilder = webClientBuilder;
    }

    @Override
    public String getToolName() {
        return "codescan";
    }

    @Override
    public String fetchAlerts(String owner, String repository, String token) {
        String url = "https://api.github.com/repos/" + owner + "/" + repository + "/code-scanning/alerts";
        return webClientBuilder.build()
                .get()
                .uri(url)
                .header("Authorization", "Bearer " + token)
                .retrieve()
                .bodyToMono(String.class)
                .block();
    }
}
