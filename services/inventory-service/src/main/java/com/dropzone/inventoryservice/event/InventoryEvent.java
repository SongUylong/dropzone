package com.dropzone.inventoryservice.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventoryEvent {
    private String eventType; // InventoryReserved, InventoryReleased
    private String reservationId;
    private Long ticketCategoryId;
    private Integer quantity;
    private String userId;
    private Instant timestamp;
}
