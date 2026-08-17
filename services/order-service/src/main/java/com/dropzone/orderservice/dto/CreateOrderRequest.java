package com.dropzone.orderservice.dto;

import jakarta.validation.constraints.*;
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

    @NotNull(message = "eventId is required")
    private Long eventId;

    private String eventName;

    @NotNull(message = "ticketCategoryId is required")
    private Long ticketCategoryId;

    private String categoryName;

    @NotNull(message = "quantity is required")
    @Min(value = 1, message = "quantity must be at least 1")
    private Integer quantity;

    @NotNull(message = "unitPrice is required")
    @DecimalMin(value = "0.01", message = "unitPrice must be greater than 0")
    private BigDecimal unitPrice;

    private String customOrderNumber; // Optional, e.g. "DZ10239"
    private String idempotencyKey;    // Optional, for idempotent order creation
    private String reservationId;
}
