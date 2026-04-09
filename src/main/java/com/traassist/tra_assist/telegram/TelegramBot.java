package com.traassist.tra_assist.telegram;

import com.traassist.tra_assist.rag.RagResponse;
import com.traassist.tra_assist.rag.RagService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Slf4j
@Component
public class TelegramBot extends TelegramLongPollingBot {

    private final RagService ragService;

    @Value("${telegram.bot-username}")
    private String botUsername;

    public TelegramBot(@Value("${telegram.bot-token}") String botToken, RagService ragService) {
        super(botToken);
        this.ragService = ragService;
    }

    @Override
    public String getBotUsername() {
        return botUsername;
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (!update.hasMessage() || !update.getMessage().hasText()) return;

        String userText = update.getMessage().getText();
        Long chatId = update.getMessage().getChatId();
        String username = update.getMessage().getFrom().getFirstName();

        log.info("Message from {}: {}", username, userText);

        if (userText.equals("/start")) {
            send(chatId, welcomeMessage());
            return;
        }

        send(chatId, "⏳ Processing your question, please wait...");

        try {
            RagResponse response = ragService.query(userText);
            send(chatId, response.getAnswer());
        } catch (Exception e) {
            log.error("Error processing query: {}", e.getMessage());
            send(chatId, "❌ Sorry, something went wrong. Please try again.");
        }
    }

    private void send(Long chatId, String text) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId.toString());
        message.setText(text);
        try {
            execute(message);
        } catch (TelegramApiException e) {
            log.error("Failed to send message: {}", e.getMessage());
        }
    }

    private String welcomeMessage() {
        return """
                👋 Welcome to TRA Assist!
                
                I help small businesses in Tanzania understand their tax obligations.
                
                Ask me anything about:
                • What taxes you need to pay
                • How to file returns
                • Payment deadlines
                • Tax rates and calculations
                
                ⚠️ This is a guidance tool only. Always verify with TRA at www.tra.go.tz
                
                Type your question to get started.
                """;
    }
}