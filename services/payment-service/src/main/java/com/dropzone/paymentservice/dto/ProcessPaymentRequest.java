package com.dropzone.paymentservice.dto;

import com.dropzone.paymentservice.model.PaymentMode;
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
public class ProcessPaymentRequest {

    @NotBlank(message = "orderNumber is required")
    private String orderNumber;

    private String userId;

    @NotNull(message = "amount is required")
    @DecimalMin(value = "0.01", message = "amount must be greater than 0")
    private BigDecimal amount;

    private PaymentMode mode; // Optional: SUCCESS, FAILED, SLOW, TIMEOUT, DUPLICATE_CALLBACK, SERVICE_UNAVAILABLE
    private String customFailureReason;
}
