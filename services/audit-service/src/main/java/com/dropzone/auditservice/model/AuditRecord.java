package com.dropzone.auditservice.model;

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
@Table(name = "audit_records")
public class AuditRecord {
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

    @Column(columnDefinition = "TEXT")
    private String payload;

    @Column(name = "formatted_message", columnDefinition = "TEXT")
    private String formattedMessage;

    @Column(name = "received_at", nullable = false)
    private Instant receivedAt;

    @PrePersist
    protected void onCreate() {
        if (receivedAt == null) receivedAt = Instant.now();
    }
}
