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
public class TicketRecord {
    private String ticketId;
    private String orderNumber;
    private String userId;
    private String eventName;
    private String categoryName;
    private String seatNumber;
    private String eventDate;
    private String qrCodeUrl;
    private String pdfUrl;
    private Instant createdAt;
}
