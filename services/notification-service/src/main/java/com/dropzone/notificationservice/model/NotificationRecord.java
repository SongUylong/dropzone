package com.dropzone.notificationservice.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "notification_records")
public class NotificationRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String topic;

    @Column(name = "event_type", length = 100)
    private String eventType;

    @Column(name = "order_number", length = 100)
    private String orderNumber;

    @Column(name = "user_id")
    private String userId;

    @Column(name = "message_text", columnDefinition = "TEXT")
    private String messageText;

    @Column(name = "sent_at", nullable = false)
    private Instant sentAt;

    @PrePersist
    protected void onCreate() {
        if (sentAt == null) sentAt = Instant.now();
    }
}
