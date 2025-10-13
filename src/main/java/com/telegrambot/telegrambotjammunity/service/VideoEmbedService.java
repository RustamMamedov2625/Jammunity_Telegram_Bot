package com.telegrambot.telegrambotjammunity.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * СЕРВИС ДЛЯ ПРЕОБРАЗОВАНИЯ ССЫЛОК ВО ВСТРОЕННЫЕ ВИДЕО
 *
 * Этот класс отвечает за "магию" превращения обычных ссылок
 * в красивые видео-плееры прямо в Telegram чате.
 *
 * КАК ЭТО РАБОТАЕТ:
 * Telegram понимает специальный синтаксис в сообщениях от ботов.
 * Если отправить сообщение в формате [текст](ссылка) с определенными
 * настройками, Telegram покажет встроенный плеер.
 */
@Slf4j
@Service
public class VideoEmbedService {

    /**
     * ПРЕОБРАЗУЕТ ОБЫЧНУЮ ССЫЛКУ В ФОРМАТ ДЛЯ ВСТРОЕННОГО ВИДЕО
     *
     * Этот метод берет обычную ссылку (например, на YouTube)
     * и превращает ее в специальный формат, который понимает Telegram.
     *
     * @param originalLink - оригинальная ссылка на видео
     * @return отформатированная строка для встроенного видео
     */
    public String createEmbeddedVideoMessage(String originalLink) {
        log.info("Преобразуем ссылку в формат встроенного видео: {}", originalLink);

        // Определяем тип видео по домену ссылки
        String videoType = detectVideoType(originalLink);
        String title = getVideoTitle(videoType);

        // Создаем сообщение в формате для встроенного видео
        // Формат: [текст-ссылки](сама-ссылка)
        String embeddedMessage = String.format("[%s](%s)", title, originalLink);

        log.debug("Создано сообщение для встроенного видео: {}", embeddedMessage);
        return embeddedMessage;
    }

    /**
     * ОПРЕДЕЛЯЕТ ТИП ВИДЕО ПО ССЫЛКЕ
     *
     * Смотрит на домен в ссылке и понимает, с какой платформой работаем.
     * Это нужно, чтобы показывать пользователю понятный текст.
     *
     * @param link - ссылка для анализа
     * @return тип видео (youtube, tiktok, instagram)
     */
    private String detectVideoType(String link) {
        if (link.contains("youtube.com") || link.contains("youtu.be")) {
            return "youtube";
        } else if (link.contains("tiktok.com")) {
            return "tiktok";
        } else if (link.contains("instagram.com")) {
            return "instagram";
        } else {
            return "video";
        }
    }

    /**
     * СОЗДАЕТ ЗАГОЛОВОК ДЛЯ ВИДЕО В ЗАВИСИМОСТИ ОТ ТИПА
     *
     * Пользователь увидит этот текст вместо сырой ссылки.
     * Telegram затем заменит его на видео-плеер.
     *
     * @param videoType - тип видео
     * @return красивый заголовок для отображения
     */
    private String getVideoTitle(String videoType) {
        switch (videoType) {
            case "youtube":
                return "🎥 YouTube видео";
            case "tiktok":
                return "📱 TikTok видео";
            case "instagram":
                return "📸 Instagram видео";
            default:
                return "🎬 Видео";
        }
    }

    /**
     * ПРОВЕРЯЕТ, МОЖНО ЛИ СДЕЛАТЬ ВСТРОЕННОЕ ВИДЕО ИЗ ЭТОЙ ССЫЛКИ
     *
     * Не все ссылки поддерживаются Telegram для встроенного воспроизведения.
     * Этот метод проверяет, сможем ли мы показать красивый плеер.
     *
     * @param link - ссылка для проверки
     * @return true если ссылка поддерживается
     */
    public boolean supportsEmbedding(String link) {
        // Telegram поддерживает встроенное воспроизведение для этих платформ
        return link.contains("youtube.com") ||
                link.contains("youtu.be") ||
                link.contains("tiktok.com") ||
                link.contains("instagram.com");
    }
}