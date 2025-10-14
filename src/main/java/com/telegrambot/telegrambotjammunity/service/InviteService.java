// InviteService.java
package com.telegrambot.telegrambotjammunity.service;

import com.telegrambot.telegrambotjammunity.entity.Invite;
import com.telegrambot.telegrambotjammunity.entity.User;
import com.telegrambot.telegrambotjammunity.repository.InviteRepository;
import com.telegrambot.telegrambotjammunity.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
public class InviteService {

    private final InviteRepository inviteRepository;
    private final UserRepository userRepository;

    public InviteService(InviteRepository inviteRepository, UserRepository userRepository) {
        this.inviteRepository = inviteRepository;
        this.userRepository = userRepository;
    }

    /**
     * СОЗДАЕТ НОВОЕ ПРИГЛАШЕНИЕ ДЛЯ УЧАСТНИКА
     */
    public String createInvite(User inviter) {
        String token = generateUniqueToken();

        Invite invite = new Invite();
        invite.setToken(token);
        invite.setCreatedBy(inviter);
        invite.setCreatedAt(LocalDateTime.now());
        invite.setExpiresAt(LocalDateTime.now().plusDays(7)); // Действует 7 дней

        inviteRepository.save(invite);
        log.info("✅ Создано приглашение: {} для пользователя {}", token, inviter.getUsername());

        return "https://t.me/jammunity_bot?start=" + token;
    }

    /**
     * ПРОВЕРЯЕТ И ИСПОЛЬЗУЕТ ПРИГЛАШЕНИЕ
     */
    public boolean useInvite(String token, User newUser) {
        Optional<Invite> inviteOpt = inviteRepository.findByTokenAndUsedFalse(token);

        if (inviteOpt.isPresent()) {
            Invite invite = inviteOpt.get();

            // Проверяем не истекло ли приглашение
            if (invite.getExpiresAt().isBefore(LocalDateTime.now())) {
                log.warn("❌ Приглашение просрочено: {}", token);
                return false;
            }

            // Отмечаем приглашение как использованное
            invite.setUsed(true);
            invite.setUsedAt(LocalDateTime.now());
            invite.setUsedBy(newUser);
            inviteRepository.save(invite);

            // Связываем пользователей
            newUser.setInvitedBy(invite.getCreatedBy());
            newUser.setInviteToken(token);
            userRepository.save(newUser);

            log.info("✅ Приглашение использовано: {} пользователем {}", token, newUser.getUsername());
            return true;
        }

        log.warn("❌ Недействительное приглашение: {}", token);
        return false;
    }

    private String generateUniqueToken() {
        return UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}