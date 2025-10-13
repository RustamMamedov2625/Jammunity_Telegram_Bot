package com.telegrambot.telegrambotjammunity.handler;

import com.telegrambot.telegrambotjammunity.service.LinkDetectionService;
import com.telegrambot.telegrambotjammunity.service.VideoEmbedService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

/**
 * ОБРАБОТЧИК ВИДЕО-ССЫЛОК
 *
 * Этот класс "ловит" сообщения с видео-ссылками и обрабатывает их.
 * Он работает как фильтр: проверяет каждое сообщение и если видит
 * видео-ссылку - берет ее на обработку.
 */
@Slf4j
@Component
public class VideoLinkHandler {

    private final LinkDetectionService linkDetectionService;
    private final VideoEmbedService videoEmbedService;

    // КОНСТРУКТОР - Spring автоматически передает сюда нужные сервисы
    public VideoLinkHandler(LinkDetectionService linkDetectionService,
                            VideoEmbedService videoEmbedService) {
        this.linkDetectionService = linkDetectionService;
        this.videoEmbedService = videoEmbedService;
    }

    /**
     * ПРОВЕРЯЕТ, МОЖЕТ ЛИ ЭТОТ ОБРАБОТЧИК РАБОТАТЬ С СООБЩЕНИЕМ
     *
     * Этот метод вызывается для КАЖДОГО сообщения в чате.
     * Он смотрит: есть ли текст в сообщении и есть ли в нем видео-ссылки.
     * Если да - возвращает true и тогда запускается handle()
     *
     * @param message - сообщение из Telegram
     * @return true если сообщение содержит видео-ссылку
     */
    public boolean canHandle(Message message) {
        return message.hasText() &&
                linkDetectionService.containsVideoLink(message.getText());
    }

    /**
     * ОСНОВНОЙ МЕТОД ОБРАБОТКИ СООБЩЕНИЯ С ВИДЕО-ССЫЛКОЙ
     *
     * Этот метод делает всю работу:
     * 1. Извлекает ссылку из текста
     * 2. Преобразует ее в формат встроенного видео
     * 3. Отправляет обратно в чат красивое сообщение с плеером
     *
     * @param message - оригинальное сообщение от пользователя
     * @param sender - объект для отправки сообщений обратно в Telegram
     */
    public void handle(Message message, org.telegram.telegrambots.meta.bots.AbsSender sender) {
        try {
            // Шаг 1: Достаем ссылку из текста сообщения
            String videoLink = linkDetectionService.getFirstVideoLink(message.getText());

            if (videoLink != null) {
                // Шаг 2: Проверяем, поддерживается ли встроенное воспроизведение
                if (videoEmbedService.supportsEmbedding(videoLink)) {
                    // Шаг 3: Преобразуем ссылку в формат для встроенного видео
                    String embeddedMessage = videoEmbedService.createEmbeddedVideoMessage(videoLink);

                    // Шаг 4: Создаем и настраиваем сообщение для отправки
                    SendMessage response = new SendMessage();
                    response.setChatId(message.getChatId().toString()); // В какой чат отправлять
                    response.setText(embeddedMessage); // Текст сообщения
                    response.setReplyToMessageId(message.getMessageId()); // Ответ на конкретное сообщение
                    response.setParseMode("Markdown"); // ВКЛЮЧАЕМ Markdown для встроенных видео!

                    // Шаг 5: Отправляем сообщение
                    sender.execute(response);
                    log.info("✅ Отправлено встроенное видео для ссылки: {} от пользователя {}",
                            videoLink, message.getFrom().getUserName());
                } else {
                    // Если ссылка не поддерживает встроенное воспроизведение
                    log.warn("❌ Ссылка не поддерживает встроенное воспроизведение: {}", videoLink);
                }
            }
        } catch (TelegramApiException e) {
            log.error("🚨 Ошибка при отправке встроенного видео", e);
        }
    }

    /**
     * ОБРАБАТЫВАЕТ ССЫЛКИ, КОТОРЫЕ НЕ ПОДДЕРЖИВАЮТ ВСТРОЕННОЕ ВИДЕО
     *
     * Если пользователь отправил видео-ссылку, но она не поддерживается
     * Telegram для встроенного воспроизведения, мы можем отправить
     * сообщение с объяснением.
     */
    private void handleUnsupportedLink(String videoLink, Message message,
                                       org.telegram.telegrambots.meta.bots.AbsSender sender) {
        try {
            String responseText = "❌ К сожалению, эта платформа не поддерживает встроенное воспроизведение: " + videoLink;

            SendMessage response = new SendMessage();
            response.setChatId(message.getChatId().toString());
            response.setText(responseText);
            response.setReplyToMessageId(message.getMessageId());

            sender.execute(response);
            log.warn("Отправлено сообщение о неподдерживаемой ссылке: {}", videoLink);
        } catch (TelegramApiException e) {
            log.error("Ошибка при отправке сообщения о неподдерживаемой ссылке", e);
        }
    }
}