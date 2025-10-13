package com.telegrambot.telegrambotjammunity.service;

import com.telegrambot.telegrambotjammunity.config.TelegramBotConfig;
import com.telegrambot.telegrambotjammunity.handler.VideoLinkHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.Update;

@Slf4j
@Component
public class TelegramBot extends TelegramLongPollingBot {

    private final TelegramBotConfig botConfig;
    private final VideoLinkHandler videoLinkHandler;

    public TelegramBot(TelegramBotConfig botConfig, VideoLinkHandler videoLinkHandler) {
        this.botConfig = botConfig;
        this.videoLinkHandler = videoLinkHandler;
    }

    @Override
    public String getBotUsername() {
        return botConfig.getUsername();
    }

    @Override
    public String getBotToken() {
        return botConfig.getToken();
    }

    @Override
    public void onUpdateReceived(Update update) {
        log.debug("Получено обновление: {}", update);

        if (update.hasMessage()) {
            var message = update.getMessage();
            log.info("Получено сообщение от {}: {}",
                    message.getFrom().getUserName(),
                    message.getText());

            // Обработка видео-ссылок
            if (videoLinkHandler.canHandle(message)) {
                log.debug("Обнаружена видео-ссылка, запускаем обработчик");
                videoLinkHandler.handle(message, this);
            }
        }
    }
}