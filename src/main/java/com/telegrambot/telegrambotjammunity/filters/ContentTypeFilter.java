package com.telegrambot.telegrambotjammunity.filters;

import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.springframework.stereotype.Component;

@Component
public class ContentTypeFilter implements MessageFilter {
    @Override
    public boolean check(Update update) {
        if(update.hasMessage()){
            Message message = update.getMessage();
            return message.hasText() || message.hasVoice() || message.hasEntities();
        }
        return false;
    }
}
