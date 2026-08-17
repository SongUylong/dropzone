package com.dropzone.paymentservice.unit;

import com.dropzone.paymentservice.model.PaymentMode;
import com.dropzone.paymentservice.model.PaymentStatus;
import com.dropzone.paymentservice.provider.MockPayProvider;
import com.dropzone.paymentservice.provider.MockPayResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

public class PaymentValidationTest {

    private final MockPayProvider mockPayProvider = new MockPayProvider();

    @Test
    @DisplayName("Should successfully process payment in SUCCESS mode")
    void testSuccessfulPayment() {
        MockPayResponse response = mockPayProvider.processPayment("DZ9901", BigDecimal.valueOf(500.00), PaymentMode.SUCCESS, null);
        assertEquals(PaymentStatus.SUCCESS, response.getStatus());
        assertNotNull(response.getTransactionId());
        assertNull(response.getFailureReason());
        assertFalse(response.isDuplicate());
    }

    @Test
    @DisplayName("Should handle failure gracefully in FAILED mode")
    void testFailedPayment() {
        MockPayResponse response = mockPayProvider.processPayment("DZ9902", BigDecimal.valueOf(250.00), PaymentMode.FAILED, "Card declined");
        assertEquals(PaymentStatus.FAILED, response.getStatus());
        assertNull(response.getTransactionId());
        assertEquals("Card declined", response.getFailureReason());
    }

    @Test
    @DisplayName("Should simulate duplicate payment callback")
    void testDuplicateCallback() {
        MockPayResponse response = mockPayProvider.processPayment("DZ9903", BigDecimal.valueOf(100.00), PaymentMode.DUPLICATE_CALLBACK, null);
        assertEquals(PaymentStatus.SUCCESS, response.getStatus());
        assertTrue(response.isDuplicate());
    }
}
