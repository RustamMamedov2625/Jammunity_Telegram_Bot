package com.telegrambot.telegrambotjammunity.filters;

import org.hibernate.sql.Update;

public interface MessageFilter {
    boolean check(Update update);
}
