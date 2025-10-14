// UserRepository.java
package com.telegrambot.telegrambotjammunity.repository;

import com.telegrambot.telegrambotjammunity.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByInviteToken(String token);
    Optional<User> findByUsername(String username);
    List<User> findByInvitedBy(User user);

    @Query("SELECT u FROM User u WHERE u.invitedBy.telegramId = :inviterId")
    List<User> findInvitedUsers(@Param("inviterId") Long inviterId);
}

