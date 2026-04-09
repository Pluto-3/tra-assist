package com.traassist.tra_assist.rag;

import com.traassist.tra_assist.language.LanguageService;
import com.traassist.tra_assist.observability.ObservabilityService;
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
    private final ObservabilityService observabilityService;

    public RagResponse query(String userQuery) {
        long start = System.currentTimeMillis();
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

        long elapsed = System.currentTimeMillis() - start;
        observabilityService.log(userQuery, finalAnswer, confidence.name(), topSimilarity, isSwahili, elapsed);

        log.info("Query done | confidence={} similarity={} time={}ms", confidence, topSimilarity, elapsed);

        RagResponse response = new RagResponse();
        response.setAnswer(finalAnswer);
        response.setSources(chunks.stream().map(RetrievedChunk::getSourceFile).distinct().toList());
        response.setTopSimilarity(topSimilarity);
        response.setConfidence(confidence.name());

        return response;
    }
}
