package com.dropzone.inventoryservice.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReserveTicketRequest {
    private String userId;

    @NotNull(message = "eventId is required")
    private Long eventId;

    private String eventName;

    @NotNull(message = "ticketCategoryId is required")
    private Long ticketCategoryId;

    @NotNull(message = "quantity is required")
    @Min(value = 1, message = "quantity must be at least 1")
    private Integer quantity;
}
