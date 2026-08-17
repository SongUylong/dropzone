package com.dropzone.paymentservice.event;

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
public class PaymentEvent {

    private String eventType; // PaymentStarted, PaymentCompleted, PaymentFailed
    private String paymentId;
    private String orderNumber;
    private String userId;
    private BigDecimal amount;
    private String currency;
    private String status;
    private String failureReason;
    private String transactionId;
    private Instant timestamp;
}
