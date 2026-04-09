package com.traassist.tra_assist.rag;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class RagService {

    private final RetrievalService retrievalService;
    private final GenerationService generationService;
    private final ConfidenceService confidenceService;
    private final DisclaimerService disclaimerService;

    public RagResponse query(String userQuery) {
        log.info("Processing query: {}", userQuery);

        List<RetrievedChunk> chunks = retrievalService.retrieve(userQuery, 3);
        String answer = generationService.generate(userQuery, chunks);

        double topSimilarity = chunks.isEmpty() ? 0 : chunks.get(0).getSimilarity();
        ConfidenceLevel confidence = confidenceService.evaluate(topSimilarity, answer);
        String finalAnswer = disclaimerService.attach(answer, confidence);

        log.info("Confidence: {} | Similarity: {}", confidence, topSimilarity);

        RagResponse response = new RagResponse();
        response.setAnswer(finalAnswer);
        response.setSources(chunks.stream().map(RetrievedChunk::getSourceFile).distinct().toList());
        response.setTopSimilarity(topSimilarity);
        response.setConfidence(confidence.name());

        return response;
    }
}
