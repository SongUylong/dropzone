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
public class ProcessPaymentRequestDto {
    private String orderNumber;
    private BigDecimal amount;
    private String mode; // e.g. SUCCESS, FAILED, SLOW, TIMEOUT, SERVICE_UNAVAILABLE
    private String customFailureReason;
}
