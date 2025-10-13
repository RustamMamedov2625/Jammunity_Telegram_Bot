package com.telegrambot.telegrambotjammunity.dto;

import lombok.Data;
import org.telegram.telegrambots.meta.api.objects.Message;

@Data
public class MessageInfo {
    private Long chatId;
    private Integer messageId;
    private String text;
    private String fromUser;

    public static MessageInfo fromMessage(Message message) {
        MessageInfo info = new MessageInfo();
        info.setChatId(message.getChatId());
        info.setMessageId(message.getMessageId());
        info.setText(message.getText());
        info.setFromUser(message.getFrom().getUserName());
        return info;
    }
}