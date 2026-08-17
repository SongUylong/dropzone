package com.dropzone.orderservice.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderEvent {
    private String eventType; // OrderCreated, OrderConfirmed
    private Long orderId;
    private String orderNumber;
    private String userId;
    private Long eventId;
    private BigDecimal totalAmount;
    private String status;
    private Instant timestamp;
}
