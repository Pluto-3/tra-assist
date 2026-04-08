package com.traassist.tra_assist.document;

import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.model.ollama.OllamaEmbeddingModel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.ResourceUtils;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class IngestionService implements ApplicationRunner {

    private final PdfExtractorService pdfExtractor;
    private final ChunkingService chunkingService;
    private final DocumentChunkRepository repository;
    private final OllamaEmbeddingModel embeddingModel;
    private final JdbcTemplate jdbcTemplate;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        File docsDir = ResourceUtils.getFile("classpath:documents");
        File[] pdfs = docsDir.listFiles((d, name) -> name.endsWith(".pdf"));

        if (pdfs == null || pdfs.length == 0) {
            log.info("No PDF documents found in resources/documents/");
            return;
        }

        for (File pdf : pdfs) {
            if (repository.existsBySourceFile(pdf.getName())) {
                log.info("Skipping already ingested: {}", pdf.getName());
                continue;
            }
            log.info("Ingesting: {}", pdf.getName());
            ingest(pdf);
        }
    }

    private void ingest(File pdf) throws Exception {
        String text = pdfExtractor.extract(pdf);
        List<String> chunks = chunkingService.chunk(text);

        for (int i = 0; i < chunks.size(); i++) {
            String chunkText = chunks.get(i);

            DocumentChunk chunk = new DocumentChunk();
            chunk.setSourceFile(pdf.getName());
            chunk.setChunkIndex(i);
            chunk.setContent(chunkText);
            DocumentChunk saved = repository.save(chunk);

            Embedding embedding = embeddingModel.embed(chunkText).content();
            String vectorLiteral = toVectorLiteral(embedding.vector());

            jdbcTemplate.update(
                    "UPDATE document_chunks SET embedding = ?::vector WHERE id = ?",
                    vectorLiteral, saved.getId()
            );

            log.info("Saved chunk {}/{} from {}", i + 1, chunks.size(), pdf.getName());
        }
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