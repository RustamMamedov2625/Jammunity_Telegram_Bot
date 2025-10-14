package com.telegrambot.telegrambotjammunity.handler;

import com.telegrambot.telegrambotjammunity.service.LinkDetectionService;
import com.telegrambot.telegrambotjammunity.service.VideoDownloadService;
import com.telegrambot.telegrambotjammunity.service.VideoEmbedService;
import com.telegrambot.telegrambotjammunity.service.VideoProcessingService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.File;

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
    private final VideoDownloadService videoDownloadService;// ЗАМЕНИЛИ VideoEmbedService
    private final VideoEmbedService videoEmbedService;


    // КОНСТРУКТОР - Spring автоматически передает сюда нужные сервисы
    public VideoLinkHandler(LinkDetectionService linkDetectionService,
                            VideoDownloadService videoDownloadService, VideoEmbedService videoEmbedService) { // ОБНОВИЛИ КОНСТРУКТОР
        this.linkDetectionService = linkDetectionService;
        this.videoDownloadService = videoDownloadService;
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
            String videoLink = linkDetectionService.getFirstVideoLink(message.getText());

            if (videoLink != null) {
                log.info("🔍 Обнаружена видео-ссылка: {}", videoLink);

                // Пытаемся скачать и отправить видео
                if (videoDownloadService.canDownloadFromPlatform(videoLink)) {
                    log.info("🔄 Запускаем скачивание и отправку видео...");

                    // Шаг 1: Скачиваем видео
                    File downloadedVideo = videoDownloadService.downloadVideo(videoLink);

                    if (downloadedVideo != null && downloadedVideo.exists()) {
                        // Шаг 2: Отправляем реальное видео файлом
                        videoDownloadService.sendVideoAsFile(downloadedVideo, message.getChatId(), sender, videoLink);
                        log.info("✅ Видео успешно скачано и отправлено");
                    } else {
                        log.error("❌ Не удалось скачать видео, используем встроенный плеер");
                        sendEmbeddedVideo(videoLink, message, sender);
                    }
                } else {
                    // Если скачивание отключено или платформа не поддерживается
                    log.info("ℹ️ Используем встроенный плеер для: {}", videoLink);
                    sendEmbeddedVideo(videoLink, message, sender);
                }
            }
        } catch (Exception e) {
            log.error("🚨 Критическая ошибка при обработке видео-ссылки", e);

            // В случае ошибки пытаемся отправить сообщение об ошибке
            try {
                SendMessage errorMsg = new SendMessage();
                errorMsg.setChatId(message.getChatId().toString());
                errorMsg.setText("❌ Произошла ошибка при обработке видео. Попробуйте позже.");
                errorMsg.setReplyToMessageId(message.getMessageId());
                sender.execute(errorMsg);
            } catch (TelegramApiException ex) {
                log.error("🚨 Не удалось отправить сообщение об ошибке", ex);
            }
        }
    }

    // Вспомогательный метод для отправки встроенного видео
    private void sendEmbeddedVideo(String videoLink, Message message,
                                   org.telegram.telegrambots.meta.bots.AbsSender sender) {
        try {
            String embeddedMessage = videoEmbedService.createEmbeddedVideoMessage(videoLink);

            SendMessage response = new SendMessage();
            response.setChatId(message.getChatId().toString());
            response.setText(embeddedMessage);
            response.setReplyToMessageId(message.getMessageId());
            response.setParseMode("Markdown");

            sender.execute(response);
            log.info("✅ Отправлено встроенное видео");

        } catch (TelegramApiException e) {
            log.error("❌ Ошибка отправки встроенного видео", e);
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