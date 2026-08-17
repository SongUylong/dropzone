package com.dropzone.paymentservice.dto;

import com.dropzone.paymentservice.model.PaymentMode;
import com.dropzone.paymentservice.model.PaymentStatus;
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
public class PaymentDto {

    private Long id;
    private String paymentId;
    private String orderNumber;
    private BigDecimal amount;
    private String currency;
    private PaymentStatus status;
    private PaymentMode mode;
    private String failureReason;
    private String transactionId;
    private Instant createdAt;
    private Instant updatedAt;
    private String formattedUserView;

    public static String buildFormattedUserView(String orderNumber, BigDecimal amount, PaymentStatus status, String failureReason) {
        String num = orderNumber != null ? (orderNumber.startsWith("#") ? orderNumber.substring(1) : orderNumber) : "";
        String amountFormatted = amount != null ? "$" + amount.stripTrailingZeros().toPlainString() : "$0";
        
        StringBuilder sb = new StringBuilder();
        sb.append("Payment\n\n\nOrder:\n").append(num)
          .append("\n\n\nAmount:\n").append(amountFormatted)
          .append("\n\n\nStatus:\n").append(status != null ? status.name() : "PENDING");

        if (failureReason != null && !failureReason.isBlank()) {
            sb.append("\n\n\nReason:\n").append(failureReason);
        }

        return sb.toString();
    }
}
