package com.traassist.tra_assist.rag;

import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.ollama.OllamaChatModel;
import dev.langchain4j.model.output.Response;
import dev.langchain4j.data.message.AiMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class GenerationService {

    private final OllamaChatModel chatModel;
    private final RestTemplate restTemplate;

    @Value("${ollama.base-url}")
    private String ollamaBaseUrl;

    @Value("${ollama.embedding-model}")
    private String embeddingModel;

    public String generate(String query, List<RetrievedChunk> chunks) {
        unloadEmbeddingModel();
        String context = buildContext(chunks);
        String prompt = buildPrompt(query, context);
        log.info("Sending prompt to Mistral. Context chunks: {}", chunks.size());
        Response<AiMessage> response = chatModel.generate(UserMessage.from(prompt));
        return response.content().text();
    }

    private void unloadEmbeddingModel() {
        try {
            log.info("Unloading embedding model to free RAM...");
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            Map<String, Object> body = Map.of(
                    "model", embeddingModel,
                    "keep_alive", 0
            );
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);
            restTemplate.postForEntity(ollamaBaseUrl + "/api/embeddings", request, String.class);
            Thread.sleep(1000);
            log.info("Embedding model unloaded.");
        } catch (Exception e) {
            log.warn("Could not unload embedding model: {}", e.getMessage());
        }
    }

    private String buildContext(List<RetrievedChunk> chunks) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < chunks.size(); i++) {
            RetrievedChunk chunk = chunks.get(i);
            sb.append("Source ").append(i + 1).append(" (").append(chunk.getSourceFile()).append("):\n");
            sb.append(chunk.getContent()).append("\n\n");
        }
        return sb.toString();
    }

    private String buildPrompt(String query, String context) {
        return """
                You are TRA Assist, a tax guidance assistant for small businesses in Tanzania.
                You ONLY answer based on the context provided below. Do not use any outside knowledge.
                If the context does not contain enough information, say so clearly.
                Always use simple, plain language. Avoid legal jargon.
                
                CONTEXT:
                %s
                
                QUESTION: %s
                
                Respond using EXACTLY this format and no other format:
                
                ANSWER:
                [Your direct answer here]
                
                WHAT THIS MEANS:
                [Simple explanation of what this means for the business owner]
                
                WHAT TO DO NEXT:
                1. [First action step]
                2. [Second action step]
                3. [Third action step if needed]
                
                CONFIDENCE: [Write only HIGH, MEDIUM, or LOW]
                HIGH = context directly and clearly answers the question
                MEDIUM = context partially answers the question
                LOW = context does not clearly answer the question
                """.formatted(context, query);
    }
}