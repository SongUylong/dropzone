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
public class PaymentResponseDto {
    private Long id;
    private String paymentId;
    private String orderNumber;
    private BigDecimal amount;
    private String currency;
    private String status;
    private String mode;
    private String failureReason;
    private String transactionId;
    private String formattedUserView;
    private boolean fallbackExecuted;
}
