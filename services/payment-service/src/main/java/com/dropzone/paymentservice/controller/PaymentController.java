package com.dropzone.paymentservice.controller;

import jakarta.validation.Valid;
import com.dropzone.paymentservice.dto.ChaosConfigDto;
import com.dropzone.paymentservice.dto.PaymentCallbackRequest;
import com.dropzone.paymentservice.dto.PaymentDto;
import com.dropzone.paymentservice.dto.ProcessPaymentRequest;
import com.dropzone.paymentservice.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/process")
    public ResponseEntity<PaymentDto> processPayment(
            @Valid @RequestBody ProcessPaymentRequest request,
            @RequestHeader(value = "X-User-Id", required = false) String xUserIdHeader) {
        if ((request.getUserId() == null || request.getUserId().isBlank()) && xUserIdHeader != null && !xUserIdHeader.isBlank()) {
            request.setUserId(xUserIdHeader);
        }
        PaymentDto payment = paymentService.processPayment(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(payment);
    }

    @PostMapping("/callback")
    public ResponseEntity<PaymentDto> handleCallback(@RequestBody PaymentCallbackRequest callback) {
        PaymentDto payment = paymentService.handleCallback(callback);
        return ResponseEntity.ok(payment);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PaymentDto> getPaymentById(@PathVariable Long id) {
        return ResponseEntity.ok(paymentService.getPaymentById(id));
    }

    @GetMapping("/number/{orderNumber}")
    public ResponseEntity<PaymentDto> getPaymentByOrderNumber(@PathVariable String orderNumber) {
        return ResponseEntity.ok(paymentService.getPaymentByOrderNumber(orderNumber));
    }

    @GetMapping("/number/{orderNumber}/view")
    public ResponseEntity<String> getFormattedUserView(@PathVariable String orderNumber) {
        return ResponseEntity.ok(paymentService.getFormattedUserViewByOrderNumber(orderNumber));
    }

    @GetMapping("/chaos")
    public ResponseEntity<ChaosConfigDto> getChaosConfig() {
        return ResponseEntity.ok(paymentService.getChaosConfig());
    }

    @PostMapping("/chaos")
    public ResponseEntity<ChaosConfigDto> updateChaosConfig(@RequestBody ChaosConfigDto config) {
        return ResponseEntity.ok(paymentService.updateChaosConfig(config));
    }
}
