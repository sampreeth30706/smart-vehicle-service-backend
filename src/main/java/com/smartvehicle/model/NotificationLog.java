package com.smartvehicle.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "notification_logs")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class NotificationLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long recipientUserId;
    private String channel;
    private String alertType;
    private String title;

    @Column(columnDefinition = "TEXT")
    private String message;

    private boolean isRead = false;
    private LocalDateTime sentAt = LocalDateTime.now();
}
