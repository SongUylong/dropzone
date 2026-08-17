package com.dropzone.orderservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateOrderRequest {

    private String userId;
    private Long eventId;
    private String eventName;
    private Long ticketCategoryId;
    private String categoryName;
    private Integer quantity;
    private BigDecimal unitPrice;
    private String customOrderNumber; // Optional, e.g. "DZ10239"
    private String idempotencyKey;    // Optional, for idempotent order creation
    private String reservationId;
}
