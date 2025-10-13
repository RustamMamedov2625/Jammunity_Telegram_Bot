package com.telegrambot.telegrambotjammunity.config;

import com.telegrambot.telegrambotjammunity.service.TelegramBot;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

@PropertySource("classpath:application-secrets.properties")
@Configuration
public class BotConfig {

    @Bean
    public TelegramBotsApi telegramBotsApi(TelegramBot telegramBot) throws TelegramApiException {
        TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
        try {
            botsApi.registerBot(telegramBot);
        } catch (TelegramApiException e) {
            throw new RuntimeException("Ошибка регистрации бота", e);
        }
        return botsApi;
    }
}