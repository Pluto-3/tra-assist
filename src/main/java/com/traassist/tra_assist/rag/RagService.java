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

    public RagResponse query(String userQuery) {
        log.info("Processing query: {}", userQuery);

        List<RetrievedChunk> chunks = retrievalService.retrieve(userQuery, 3);
        String answer = generationService.generate(userQuery, chunks);

        RagResponse response = new RagResponse();
        response.setAnswer(answer);
        response.setSources(chunks.stream().map(RetrievedChunk::getSourceFile).distinct().toList());
        response.setTopSimilarity(chunks.isEmpty() ? 0 : chunks.get(0).getSimilarity());

        log.info("Query processed. Top similarity: {}", response.getTopSimilarity());
        return response;
    }
}
