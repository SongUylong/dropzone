package com.dropzone.inventoryservice.dto;

import com.dropzone.inventoryservice.model.TicketReservation.ReservationStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReservationResponseDto {
    private String reservationId;
    private String userId;
    private Long eventId;
    private Long ticketCategoryId;
    private String categoryName;
    private Integer quantity;
    private Instant createdAt;
    private Instant expiresAt;
    private ReservationStatus status;
}
