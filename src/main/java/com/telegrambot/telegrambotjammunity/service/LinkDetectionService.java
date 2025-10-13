package com.telegrambot.telegrambotjammunity.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.regex.Pattern;

@Slf4j
@Service
public class LinkDetectionService {

    // Регулярные выражения для определения видео-ссылок
    private static final Pattern YOUTUBE_PATTERN =
            Pattern.compile("(https?://)?(www\\.)?(youtube\\.com|youtu\\.be)/.+", Pattern.CASE_INSENSITIVE);
    private static final Pattern TIKTOK_PATTERN =
            Pattern.compile("(https?://)?(www\\.)?tiktok\\.com/.+", Pattern.CASE_INSENSITIVE);
    private static final Pattern INSTAGRAM_PATTERN =
            Pattern.compile("(https?://)?(www\\.)?instagram\\.com/.+", Pattern.CASE_INSENSITIVE);

    public boolean containsVideoLink(String text) {
        if (text == null || text.trim().isEmpty()) {
            return false;
        }

        boolean hasLink = YOUTUBE_PATTERN.matcher(text).find() ||
                TIKTOK_PATTERN.matcher(text).find() ||
                INSTAGRAM_PATTERN.matcher(text).find();

        log.debug("Проверка ссылок в тексте: '{}' - результат: {}", text, hasLink);
        return hasLink;
    }

    public String getFirstVideoLink(String text) {
        if (!containsVideoLink(text)) {
            return null;
        }

        // Простая реализация - ищем первую ссылку в тексте
        String[] words = text.split("\\s+");
        for (String word : words) {
            if (YOUTUBE_PATTERN.matcher(word).find() ||
                    TIKTOK_PATTERN.matcher(word).find() ||
                    INSTAGRAM_PATTERN.matcher(word).find()) {
                log.debug("Найдена видео-ссылка: {}", word);
                return word;
            }
        }
        return null;
    }
}