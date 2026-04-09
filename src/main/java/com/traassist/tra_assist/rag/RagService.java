package com.traassist.tra_assist.rag;

import com.traassist.tra_assist.language.LanguageService;
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
    private final LanguageService languageService;

    public RagResponse query(String userQuery) {
        log.info("Processing query: {}", userQuery);

        boolean isSwahili = languageService.isSwahili(userQuery);
        String processedQuery = isSwahili ? languageService.translateToEnglish(userQuery) : userQuery;

        if (isSwahili) log.info("Swahili detected. Translated query: {}", processedQuery);

        List<RetrievedChunk> chunks = retrievalService.retrieve(userQuery, 3);
        String answer = generationService.generate(userQuery, chunks);

        double topSimilarity = chunks.isEmpty() ? 0 : chunks.get(0).getSimilarity();
        ConfidenceLevel confidence = confidenceService.evaluate(topSimilarity, answer);
        String finalAnswer = disclaimerService.attach(answer, confidence);

        if (isSwahili) finalAnswer = languageService.translateToSwahili(finalAnswer);

        log.info("Confidence: {} | Similarity: {}", confidence, topSimilarity, isSwahili ? "sw" : "en");

        RagResponse response = new RagResponse();
        response.setAnswer(finalAnswer);
        response.setSources(chunks.stream().map(RetrievedChunk::getSourceFile).distinct().toList());
        response.setTopSimilarity(topSimilarity);
        response.setConfidence(confidence.name());

        return response;
    }
}
