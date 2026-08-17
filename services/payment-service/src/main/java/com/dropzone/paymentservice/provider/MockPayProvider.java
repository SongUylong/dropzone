package com.dropzone.paymentservice.provider;

import com.dropzone.paymentservice.model.PaymentMode;
import com.dropzone.paymentservice.model.PaymentStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.UUID;

@Slf4j
@Component
public class MockPayProvider {

    public MockPayResponse processPayment(String orderNumber, BigDecimal amount, PaymentMode mode, String customFailureReason) {
        PaymentMode activeMode = mode != null ? mode : PaymentMode.SUCCESS;
        log.info("MockPay executing payment for Order {} with Mode {}", orderNumber, activeMode);

        String txnId = "TXN_" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        switch (activeMode) {
            case SUCCESS:
                return MockPayResponse.builder()
                        .transactionId(txnId)
                        .status(PaymentStatus.SUCCESS)
                        .failureReason(null)
                        .isDuplicate(false)
                        .build();

            case FAILED:
                String reason = customFailureReason != null && !customFailureReason.isBlank() 
                        ? customFailureReason 
                        : "Card declined";
                return MockPayResponse.builder()
                        .transactionId(null)
                        .status(PaymentStatus.FAILED)
                        .failureReason(reason)
                        .isDuplicate(false)
                        .build();

            case SLOW:
                try {
                    log.info("MockPay SLOW mode: delaying response by 3000ms...");
                    Thread.sleep(3000);
                } catch (InterruptedException ignored) {
                }
                return MockPayResponse.builder()
                        .transactionId(txnId)
                        .status(PaymentStatus.SUCCESS)
                        .failureReason(null)
                        .isDuplicate(false)
                        .build();

            case TIMEOUT:
                return MockPayResponse.builder()
                        .transactionId(null)
                        .status(PaymentStatus.TIMEOUT)
                        .failureReason("Payment gateway response timed out")
                        .isDuplicate(false)
                        .build();

            case SERVICE_UNAVAILABLE:
                String unavailReason = customFailureReason != null && !customFailureReason.isBlank()
                        ? customFailureReason
                        : "Provider unavailable";
                return MockPayResponse.builder()
                        .transactionId(null)
                        .status(PaymentStatus.FAILED)
                        .failureReason(unavailReason)
                        .isDuplicate(false)
                        .build();

            case DUPLICATE_CALLBACK:
                return MockPayResponse.builder()
                        .transactionId("TXN_DUPLICATE_888")
                        .status(PaymentStatus.SUCCESS)
                        .failureReason(null)
                        .isDuplicate(true)
                        .build();

            default:
                return MockPayResponse.builder()
                        .transactionId(txnId)
                        .status(PaymentStatus.SUCCESS)
                        .build();
        }
    }
}
