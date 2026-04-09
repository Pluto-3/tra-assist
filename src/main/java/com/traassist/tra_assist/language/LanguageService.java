package com.traassist.tra_assist.language;

import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.ollama.OllamaChatModel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class LanguageService {

    private final OllamaChatModel chatModel;

    public boolean isSwahili(String text) {
        String lower = text.toLowerCase();
        String[] swahiliMarkers = {"ni", "ya", "wa", "na", "kwa", "je", "gani", "nini", "vipi", "ninaweza", "biashara", "kodi", "malipo", "serikali"};
        int hits = 0;
        for (String marker : swahiliMarkers) {
            if (lower.contains(" " + marker + " ") || lower.startsWith(marker + " ")) hits++;
        }
        return hits >= 2;
    }

    public String translateToEnglish(String swahiliText) {
        log.info("Translating query to English...");
        String prompt = "Translate the following Swahili text to English. Return only the translation, nothing else.\n\n" + swahiliText;
        return chatModel.generate(UserMessage.from(prompt)).content().text().trim();
    }

    public String translateToSwahili(String englishText) {
        log.info("Translating response to Swahili...");
        String prompt = "Translate the following English text to Swahili. Keep the formatting exactly as is. Return only the translation, nothing else.\n\n" + englishText;
        return chatModel.generate(UserMessage.from(prompt)).content().text().trim();
    }
}
