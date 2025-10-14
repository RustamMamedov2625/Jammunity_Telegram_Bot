// InviteRepository.java
package com.telegrambot.telegrambotjammunity.repository;

import com.telegrambot.telegrambotjammunity.entity.Invite;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface InviteRepository extends JpaRepository<Invite, Long> {
    Optional<Invite> findByToken(String token);
    Optional<Invite> findByTokenAndUsedFalse(String token);
}