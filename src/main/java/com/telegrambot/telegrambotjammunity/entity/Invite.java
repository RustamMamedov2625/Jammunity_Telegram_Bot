// Invite.java
package com.telegrambot.telegrambotjammunity.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "invites")
@Data
public class Invite {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String token;
    private LocalDateTime createdAt;
    private LocalDateTime expiresAt;
    private boolean used = false;
    private LocalDateTime usedAt;

    @ManyToOne
    @JoinColumn(name = "created_by_id")
    private User createdBy; // Кто создал приглашение

    @OneToOne
    @JoinColumn(name = "used_by_id")
    private User usedBy; // Кто использовал приглашение
}