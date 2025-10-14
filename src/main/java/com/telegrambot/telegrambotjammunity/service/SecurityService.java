// SecurityService.java
package com.telegrambot.telegrambotjammunity.service;

import com.telegrambot.telegrambotjammunity.entity.User;
import com.telegrambot.telegrambotjammunity.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
public class SecurityService {

    private final UserRepository userRepository;
    private final ConcurrentHashMap<Long, Integer> userInviteLimits = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Long, LocalDateTime> userLastInvite = new ConcurrentHashMap<>();

    // Лимиты: максимум 5 приглашений в день
    private static final int DAILY_INVITE_LIMIT = 5;

    public SecurityService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * ПРОВЕРЯЕТ МОЖЕТ ЛИ ПОЛЬЗОВАТЕЛЬ СОЗДАТЬ ЕЩЕ ПРИГЛАШЕНИЕ
     */
    public boolean canCreateInvite(User user) {
        Long userId = user.getTelegramId();
        LocalDateTime now = LocalDateTime.now();

        // Сбрасываем счетчик если прошел день
        LocalDateTime lastInvite = userLastInvite.get(userId);
        if (lastInvite != null && lastInvite.plusDays(1).isBefore(now)) {
            userInviteLimits.remove(userId);
            userLastInvite.remove(userId);
        }

        // Проверяем лимит
        int currentCount = userInviteLimits.getOrDefault(userId, 0);
        if (currentCount >= DAILY_INVITE_LIMIT) {
            log.warn("⚠️ Достигнут лимит приглашений для пользователя: {}", user.getUsername());
            return false;
        }

        // Обновляем счетчик
        userInviteLimits.put(userId, currentCount + 1);
        userLastInvite.put(userId, now);
        return true;
    }

    /**
     * ПОЛУЧАЕТ ЦЕПОЧКУ ПРИГЛАШЕНИЙ ДЛЯ АДМИНА
     */
    public String getInvitationChain(Long userId) {
        StringBuilder chain = new StringBuilder();
        User currentUser = userRepository.findById(userId).orElse(null);

        if (currentUser == null) {
            return "❌ Пользователь не найден";
        }

        chain.append("🔗 Цепочка приглашений:\n\n");
        chain.append(getUserInfo(currentUser)).append(" 👈 Текущий\n");

        User inviter = currentUser.getInvitedBy();
        int level = 1;

        while (inviter != null) {
            chain.append("⬆️ Уровень ").append(level).append(": ")
                    .append(getUserInfo(inviter)).append("\n");
            inviter = inviter.getInvitedBy();
            level++;
        }

        return chain.toString();
    }

    private String getUserInfo(User user) {
        return String.format("@%s (%s %s)",
                user.getUsername() != null ? user.getUsername() : "нет username",
                user.getFirstName() != null ? user.getFirstName() : "",
                user.getLastName() != null ? user.getLastName() : "");
    }
}