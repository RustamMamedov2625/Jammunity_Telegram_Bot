package com.telegrambot.telegrambotjammunity.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.File;

/**
 * УЛУЧШЕННЫЙ СЕРВИС ДЛЯ ОБРАБОТКИ ВИДЕО
 *
 * Этот класс предоставляет несколько стратегий обработки видео:
 * 1. Встроенные плееры (основной способ) - БЫСТРО и БЕСПЛАТНО
 * 2. Скачивание как запасной вариант (если нужно)
 *
 * Сейчас мы реализуем только встроенные плееры, но архитектура
 * позволяет легко добавить скачивание в будущем при необходимости.
 */
@Slf4j
@Service
public class VideoProcessingService {

    private final VideoEmbedService videoEmbedService;
    private final VideoDownloadService videoDownloadService;

    public VideoProcessingService(VideoEmbedService videoEmbedService,
                                  VideoDownloadService videoDownloadService) {
        this.videoEmbedService = videoEmbedService;
        this.videoDownloadService = videoDownloadService;
    }

    /**
     * ОБРАБАТЫВАЕТ ВИДЕО-ССЫЛКУ С ВЫБОРОМ СТРАТЕГИИ
     */
    public ProcessResult processVideoLink(String videoLink) {
        log.info("Обрабатываем видео-ссылку: {}", videoLink);

        // СТРАТЕГИЯ 1: Встроенные плееры (приоритет)
        if (videoEmbedService.supportsEmbedding(videoLink)) {
            String embeddedMessage = videoEmbedService.createEmbeddedVideoMessage(videoLink);
            return new ProcessResult(embeddedMessage, Strategy.EMBEDDED);
        }

        // СТРАТЕГИЯ 2: Скачивание (если включено и поддерживается)
        if (videoDownloadService.canDownloadFromPlatform(videoLink)) {
            log.info("🔄 Используем стратегию скачивания для: {}", videoLink);
            return new ProcessResult(videoLink, Strategy.DOWNLOAD);
        }

        // СТРАТЕГИЯ 3: Запасной вариант
        log.warn("⚠️ Ссылка не поддерживается: {}", videoLink);
        String fallbackMessage = "🔗 Ссылка на видео: " + videoLink;
        return new ProcessResult(fallbackMessage, Strategy.FALLBACK);
    }

    /**
     * ВЫПОЛНЯЕТ СКАЧИВАНИЕ И ОТПРАВКУ ВИДЕО
     */
    public void executeDownload(String videoLink, Long chatId,
                                org.telegram.telegrambots.meta.bots.AbsSender sender) {
        File downloadedVideo = videoDownloadService.downloadVideo(videoLink);
        if (downloadedVideo != null) {
            videoDownloadService.sendVideoAsFile(downloadedVideo, chatId, sender, videoLink);
        } else {
            log.error("❌ Не удалось скачать видео: {}", videoLink);
        }
    }

    // DTO для результата обработки
    public static class ProcessResult {
        public final String message;
        public final Strategy strategy;

        public ProcessResult(String message, Strategy strategy) {
            this.message = message;
            this.strategy = strategy;
        }
    }

    public enum Strategy {
        EMBEDDED, DOWNLOAD, FALLBACK
    }
}