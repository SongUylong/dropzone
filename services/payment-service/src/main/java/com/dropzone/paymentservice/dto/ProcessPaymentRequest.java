package com.dropzone.paymentservice.dto;

import com.dropzone.paymentservice.model.PaymentMode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProcessPaymentRequest {

    private String orderNumber;
    private String userId;
    private BigDecimal amount;
    private PaymentMode mode; // Optional: SUCCESS, FAILED, SLOW, TIMEOUT, DUPLICATE_CALLBACK, SERVICE_UNAVAILABLE
    private String customFailureReason;
}
