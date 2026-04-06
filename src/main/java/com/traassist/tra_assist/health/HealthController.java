package com.traassist.tra_assist.health;

import dev.langchain4j.model.ollama.OllamaEmbeddingModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class HealthController {

    @Autowired
    private OllamaEmbeddingModel embeddingModel;

    @GetMapping("/health")
    public Map<String, String> health() {
        embeddingModel.embed("test");
        return Map.of(
                "status", "ok",
                "ollama", "reachable",
                "db", "connected"
        );
    }
}
