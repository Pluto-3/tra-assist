package com.traassist.tra_assist.observability;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class ObservabilityService {

    private final QueryLogRepository repository;

    public void log(String query, String answer, String confidence, double similarity, boolean swahili, long responseTimeMs) {

        try {
            QueryLog entry = new QueryLog();
            entry.setQuery(query);
            entry.setAnswer(answer);
            entry.setConfidence(confidence);
            entry.setTopSimilarity(similarity);
            entry.setSwahili(swahili);
            entry.setResponseTimeMs(responseTimeMs);
            entry.setCreatedAt(LocalDateTime.now());
            repository.save(entry);
            log.info("Query logged | confidence={} similarity={} time={}ms", confidence, similarity, responseTimeMs);
        } catch (Exception e)  {
            log.error("Failed to log query: {}", e.getMessage());
        }
    }
}
