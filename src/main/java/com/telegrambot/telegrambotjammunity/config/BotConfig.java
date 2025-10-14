package com.telegrambot.telegrambotjammunity.config;

import com.telegrambot.telegrambotjammunity.service.TelegramBot;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

@PropertySource("classpath:application-secrets.properties")
@Slf4j
@Configuration
public class BotConfig {

    @Bean
    public TelegramBotsApi telegramBotsApi(TelegramBot telegramBot) {
        try {
            log.info("🔄 Регистрируем бота в Telegram API...");
            TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
            botsApi.registerBot(telegramBot);
            log.info("✅ Бот успешно зарегистрирован и запущен");
            return botsApi;
        } catch (TelegramApiException e) {
            log.error("❌ Ошибка регистрации бота: {}", e.getMessage());
            throw new RuntimeException("Не удалось зарегистрировать бота", e);
        }
    }
}

