package com.telegrambot.telegrambotjammunity.handler;

import com.telegrambot.telegrambotjammunity.service.LinkDetectionService;
import com.telegrambot.telegrambotjammunity.service.VideoEmbedService;
import com.telegrambot.telegrambotjammunity.service.VideoProcessingService;
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
    private final VideoProcessingService videoProcessingService;// ЗАМЕНИЛИ VideoEmbedService


    // КОНСТРУКТОР - Spring автоматически передает сюда нужные сервисы
    public VideoLinkHandler(LinkDetectionService linkDetectionService,
                            VideoProcessingService videoProcessingService) { // ОБНОВИЛИ КОНСТРУКТОР
        this.linkDetectionService = linkDetectionService;
        this.videoProcessingService = videoProcessingService;
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
                // ИСПОЛЬЗУЕМ НОВЫЙ СЕРВИС ОБРАБОТКИ
                String responseText = videoProcessingService.processVideoLink(videoLink);

                SendMessage response = new SendMessage();
                response.setChatId(message.getChatId().toString());
                response.setText(responseText);
                response.setReplyToMessageId(message.getMessageId());
                response.setParseMode("Markdown"); // Важно для встроенных видео!

                sender.execute(response);
                log.info("✅ Обработана видео-ссылка: {} от пользователя {}",
                        videoLink, message.getFrom().getUserName());
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