package com.telegrambot.telegrambotjammunity.filters;
import org.telegram.telegrambots.meta.api.objects.Update;

public interface MessageFilter {
    boolean check(Update update);
}
