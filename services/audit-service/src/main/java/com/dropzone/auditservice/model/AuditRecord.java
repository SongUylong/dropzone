package com.dropzone.auditservice.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditRecord {
    private Long id;
    private String topic;
    private String eventType;
    private String orderNumber;
    private String userId;
    private String payload;
    private String formattedMessage;
    private Instant receivedAt;
}
