package com.dropzone.paymentservice.provider;

import com.dropzone.paymentservice.model.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MockPayResponse {

    private String transactionId;
    private PaymentStatus status;
    private String failureReason;
    private boolean isDuplicate;
}
