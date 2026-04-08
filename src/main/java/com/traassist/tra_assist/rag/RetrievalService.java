package com.traassist.tra_assist.rag;

import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.model.ollama.OllamaEmbeddingModel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class RetrievalService {

    private final OllamaEmbeddingModel embeddingModel;
    private final JdbcTemplate jdbcTemplate;

    public List<RetrievedChunk> retrieve(String query, int topK) {
        Embedding queryEmbedding = embeddingModel.embed(query).content();
        String vectorLiteral = toVectorLiteral(queryEmbedding.vector());

        String sql = """
                SELECT id, content, source_file, chunk_index,
                       1 - (embedding <=> ?::vector) AS similarity
                FROM document_chunks
                ORDER BY embedding <=> ?::vector
                LIMIT ?
                """;

        List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql,
                vectorLiteral, vectorLiteral, topK);

        List<RetrievedChunk> chunks = new ArrayList<>();
        for (Map<String, Object> row : rows) {
            RetrievedChunk chunk = new RetrievedChunk();
            chunk.setContent((String) row.get("content"));
            chunk.setSourceFile((String) row.get("source_file"));
            chunk.setChunkIndex((int) row.get("chunk_index"));
            chunk.setSimilarity(((Number) row.get("similarity")).doubleValue());
            chunks.add(chunk);
        }

        log.info("Retrieved {} chunks for query. Top similarity: {}",
                chunks.size(), chunks.isEmpty() ? 0 : chunks.get(0).getSimilarity());
        return chunks;
    }

    private String toVectorLiteral(float[] vector) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < vector.length; i++) {
            sb.append(vector[i]);
            if (i < vector.length - 1) sb.append(",");
        }
        sb.append("]");
        return sb.toString();
    }
}