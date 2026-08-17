package com.dropzone.notificationservice.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JobPayload {
    private String jobId;
    private String jobType; // TICKET_GENERATION, EMAIL_NOTIFICATION, SMS_NOTIFICATION
    private String targetQueue;
    private String orderNumber;
    private String userId;
    private String details;
    private String status; // PENDING, PROCESSING, COMPLETED, FAILED
    private Instant createdAt;
    private Instant processedAt;
}
