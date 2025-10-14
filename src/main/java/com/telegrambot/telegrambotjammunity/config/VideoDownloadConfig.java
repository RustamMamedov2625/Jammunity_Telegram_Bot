package com.telegrambot.telegrambotjammunity.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "video.download")
public class VideoDownloadConfig {
    private boolean enabled = false; // По умолчанию отключено
    private String tempDirectory = "./temp/videos";
    private long maxFileSizeMb = 50; // Максимальный размер файла
    private int downloadTimeoutSeconds = 30;
}