package com.traassist.tra_assist.document;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class ChunkingService {

    private static final int CHUNK_SIZE = 500;
    private static final int OVERLAP = 50;

    public List<String> chunk(String text) {
        List<String> chunks = new ArrayList<>();
        String cleaned = text.replaceAll("\\s+", " ").trim();
        int start = 0;
        while (start < cleaned.length()) {
            int end = Math.min(start + CHUNK_SIZE, cleaned.length());
            chunks.add(cleaned.substring(start, end));
            start += CHUNK_SIZE - OVERLAP;
        }
        return chunks;
    }
}
