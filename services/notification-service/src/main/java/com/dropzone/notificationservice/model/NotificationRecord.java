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
public class NotificationRecord {
    private Long id;
    private String topic;
    private String eventType;
    private String orderNumber;
    private String userId;
    private String messageText;
    private Instant sentAt;
}
