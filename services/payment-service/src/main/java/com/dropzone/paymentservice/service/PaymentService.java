package com.dropzone.paymentservice.service;

import com.dropzone.paymentservice.dto.ChaosConfigDto;
import com.dropzone.paymentservice.dto.PaymentCallbackRequest;
import com.dropzone.paymentservice.dto.PaymentDto;
import com.dropzone.paymentservice.dto.ProcessPaymentRequest;

public interface PaymentService {
    PaymentDto processPayment(ProcessPaymentRequest request);
    PaymentDto handleCallback(PaymentCallbackRequest callback);
    PaymentDto getPaymentById(Long id);
    PaymentDto getPaymentByOrderNumber(String orderNumber);
    String getFormattedUserViewByOrderNumber(String orderNumber);
    ChaosConfigDto getChaosConfig();
    ChaosConfigDto updateChaosConfig(ChaosConfigDto config);
}
