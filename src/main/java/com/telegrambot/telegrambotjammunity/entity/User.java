// User.java
package com.telegrambot.telegrambotjammunity.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Data
public class User {
    @Id
    private Long telegramId;

    private String username;
    private String firstName;
    private String lastName;
    private LocalDateTime joinedAt;
    private boolean isActive = true;

    @ManyToOne
    @JoinColumn(name = "invited_by")
    private User invitedBy; // Кто пригласил

    private String inviteToken; // Уникальный токен для приглашения
}

