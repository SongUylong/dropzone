package com.dropzone.paymentservice.dto;

import com.dropzone.paymentservice.model.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentCallbackRequest {

    private String paymentId;
    private String transactionId;
    private PaymentStatus status;
    private String failureReason;
}
