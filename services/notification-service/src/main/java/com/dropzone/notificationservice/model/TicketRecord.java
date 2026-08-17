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
@Table(name = "ticket_records")
public class TicketRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ticket_id", nullable = false, unique = true, length = 100)
    private String ticketId;

    @Column(name = "order_number", nullable = false, length = 100)
    private String orderNumber;

    @Column(name = "user_id")
    private String userId;

    @Column(name = "event_name")
    private String eventName;

    @Column(name = "category_name", length = 100)
    private String categoryName;

    @Column(name = "seat_number", length = 50)
    private String seatNumber;

    @Column(name = "event_date", length = 100)
    private String eventDate;

    @Column(name = "qr_code_url", length = 512)
    private String qrCodeUrl;

    @Column(name = "pdf_url", length = 512)
    private String pdfUrl;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) createdAt = Instant.now();
    }
}
