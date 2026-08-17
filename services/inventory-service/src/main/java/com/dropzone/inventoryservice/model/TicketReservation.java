package com.dropzone.inventoryservice.model;

import lombok.*;

import java.io.Serializable;
import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TicketReservation implements Serializable {

    private String reservationId;
    private String userId;
    private Long eventId;
    private String eventName;
    private Long ticketCategoryId;
    private String categoryName;
    private Integer quantity;
    private Instant createdAt;
    private Instant expiresAt;
    private ReservationStatus status;

    public enum ReservationStatus {
        RESERVED,
        CONFIRMED,
        CANCELLED,
        EXPIRED
    }
}
