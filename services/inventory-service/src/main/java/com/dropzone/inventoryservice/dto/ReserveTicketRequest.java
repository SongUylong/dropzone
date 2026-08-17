package com.dropzone.inventoryservice.dto;

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
    private Long eventId;
    private Long ticketCategoryId;
    private Integer quantity;
}
