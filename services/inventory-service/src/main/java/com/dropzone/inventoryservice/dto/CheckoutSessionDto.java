package com.dropzone.inventoryservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CheckoutSessionDto implements Serializable {
    private String sessionId;
    private String userId;
    private Long eventId;
    private String eventName;
    private Long ticketCategoryId;
    private String categoryName;
    private Integer quantity;
    private BigDecimal totalAmount;
    private String reservationId;
    private Instant createdAt;
    private Instant expiresAt;
    private String status;
}
