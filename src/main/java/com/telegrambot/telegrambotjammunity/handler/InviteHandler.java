// InviteHandler.java
package com.telegrambot.telegrambotjammunity.handler;

import com.telegrambot.telegrambotjammunity.entity.User;
import com.telegrambot.telegrambotjammunity.service.InviteService;
import com.telegrambot.telegrambotjammunity.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.time.LocalDateTime;

@Slf4j
@Component
public class InviteHandler {

    private final InviteService inviteService;
    private final UserRepository userRepository;

    public InviteHandler(InviteService inviteService, UserRepository userRepository) {
        this.inviteService = inviteService;
        this.userRepository = userRepository;
    }

    /**
     * ОБРАБАТЫВАЕТ КОМАНДУ /invite
     */
    public void handleInviteCommand(Message message, org.telegram.telegrambots.meta.bots.AbsSender sender) {
        try {
            // Получаем или создаем пользователя
            User user = getOrCreateUser(message.getFrom());

            // Создаем приглашение
            String inviteLink = inviteService.createInvite(user);

            // Отправляем ссылку пользователю
            SendMessage response = new SendMessage();
            response.setChatId(message.getChatId().toString());
            response.setText("🎫 Ваша ссылка для приглашения:\n" + inviteLink +
                    "\n\n📋 Правила:" +
                    "\n• Ссылка действует 7 дней" +
                    "\n• Вы отвечаете за приглашенных" +
                    "\n• При нарушении правил - бан вам и приглашенному");

            sender.execute(response);

        } catch (TelegramApiException e) {
            log.error("❌ Ошибка обработки команды /invite", e);
        }
    }

    /**
     * ОБРАБАТЫВАЕТ НОВОГО ПОЛЬЗОВАТЕЛЯ ПО ССЫЛКЕ
     */
    public void handleNewUserJoin(String startParam, Message message,
                                  org.telegram.telegrambots.meta.bots.AbsSender sender) {
        try {
            User newUser = getOrCreateUser(message.getFrom());

            boolean success = inviteService.useInvite(startParam, newUser);

            SendMessage response = new SendMessage();
            response.setChatId(message.getChatId().toString());

            if (success) {
                response.setText("👋 Добро пожаловать! " +
                        "\n\n📝 Пожалуйста, представьтесь:" +
                        "\n• Ваше имя" +
                        "\n• Чем занимаетесь" +
                        "\n• Откуда узнали о нас" +
                        "\n\nЭто поможет нам познакомиться!");
            } else {
                response.setText("❌ Недействительная ссылка приглашения." +
                        "\nОбратитесь к участнику за новой ссылкой.");
            }

            sender.execute(response);

        } catch (TelegramApiException e) {
            log.error("❌ Ошибка обработки нового пользователя", e);
        }
    }

    private User getOrCreateUser(org.telegram.telegrambots.meta.api.objects.User telegramUser) {
        return userRepository.findById(telegramUser.getId())
                .orElseGet(() -> {
                    User newUser = new User();
                    newUser.setTelegramId(telegramUser.getId());
                    newUser.setUsername(telegramUser.getUserName());
                    newUser.setFirstName(telegramUser.getFirstName());
                    newUser.setLastName(telegramUser.getLastName());
                    newUser.setJoinedAt(LocalDateTime.now());
                    return userRepository.save(newUser);
                });
    }
}