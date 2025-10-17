package com.telegrambot.telegrambotjammunity;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Главный класс приложения.
 * Аннотация @SpringBootApplication включает в себя:
 * - @Configuration: указывает, что класс содержит конфигурацию Spring.
 * - @EnableAutoConfiguration: автоматически настраивает Spring Boot на основе зависимостей.
 * - @ComponentScan: сканирует пакеты на наличие компонентов (бины), таких как @Service, @Repository, @Controller.
 */
@SpringBootApplication
public class TelegramBotJammunityApplication {

    public static void main(String[] args) {
        // Запускаем Spring Boot приложение.
        SpringApplication.run(TelegramBotJammunityApplication.class, args);
    }
}