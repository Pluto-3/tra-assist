package com.traassist.tra_assist.rag;

import org.springframework.stereotype.Service;

@Service
public class ConfidenceService {

    public ConfidenceLevel evaluate(double topSimilarity, String llmResponse) {
        ConfidenceLevel retrievalSignal = fromSimilarity(topSimilarity);
        ConfidenceLevel llmSignal = fromLlmResponse(llmResponse);
        return combine(retrievalSignal, llmSignal);
    }

    private ConfidenceLevel fromSimilarity(double score) {
        if (score >= 0.80) return ConfidenceLevel.HIGH;
        if (score >= 0.65) return ConfidenceLevel.MEDIUM;
        return ConfidenceLevel.LOW;
    }

    private ConfidenceLevel fromLlmResponse(String response) {
        if (response == null) return ConfidenceLevel.LOW;
        String upper = response.toUpperCase();
        if (upper.contains("CONFIDENCE: HIGH")) return ConfidenceLevel.HIGH;
        if (upper.contains("CONFIDENCE: MEDIUM")) return ConfidenceLevel.MEDIUM;
        return ConfidenceLevel.LOW;
    }

    private ConfidenceLevel combine(ConfidenceLevel retrieval, ConfidenceLevel llm) {
        if (retrieval == ConfidenceLevel.HIGH && llm == ConfidenceLevel.HIGH) return ConfidenceLevel.HIGH;
        if (retrieval == ConfidenceLevel.LOW && llm == ConfidenceLevel.LOW) return ConfidenceLevel.LOW;
        return ConfidenceLevel.MEDIUM;
    }
}
