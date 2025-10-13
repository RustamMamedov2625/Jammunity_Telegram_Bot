package com.telegrambot.telegrambotjammunity.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

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

    public VideoProcessingService(VideoEmbedService videoEmbedService) {
        this.videoEmbedService = videoEmbedService;
    }

    /**
     * ОСНОВНОЙ МЕТОД ОБРАБОТКИ ВИДЕО-ССЫЛКИ
     *
     * Пытается использовать встроенные плееры.
     * В будущем можно добавить fallback на скачивание.
     *
     * @param videoLink - ссылка на видео
     * @return текст сообщения для отправки в Telegram
     */
    public String processVideoLink(String videoLink) {
        log.info("Обрабатываем видео-ссылку: {}", videoLink);

        // СТРАТЕГИЯ 1: Встроенные плееры (рекомендуемая)
        if (videoEmbedService.supportsEmbedding(videoLink)) {
            log.debug("Используем встроенный плеер для: {}", videoLink);
            return videoEmbedService.createEmbeddedVideoMessage(videoLink);
        }

        // СТРАТЕГИЯ 2: Запасной вариант - просто показываем ссылку
        // В будущем здесь можно добавить скачивание
        log.warn("Ссылка не поддерживает встроенное воспроизведение: {}", videoLink);
        return "🔗 Ссылка на видео: " + videoLink +
                "\n\nℹ️ Для просмотра перейдите по ссылке";
    }

    /**
     * ПРОВЕРЯЕТ, НУЖНО ЛИ СКАЧИВАТЬ ВИДЕО
     *
     * Этот метод определяет, когда нам может понадобиться
     * скачивание вместо встроенного плеера.
     *
     * Сейчас всегда возвращает false, но в будущем можно добавить логику:
     * - Если платформа не поддерживает встроенные видео
     * - Если пользователь запросил скачивание
     * - Для определенных типов контента
     */
    public boolean shouldDownloadVideo(String videoLink) {
        // Сейчас отключаем скачивание - используем только встроенные плееры
        return false;

        // Пример будущей логики:
        // return !videoEmbedService.supportsEmbedding(videoLink) ||
        //        videoLink.contains("private-video.com") ||
        //        userRequestedDownload;
    }
}