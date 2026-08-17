package com.dropzone.orderservice.client;

import com.dropzone.orderservice.dto.PaymentResponseDto;
import com.dropzone.orderservice.dto.ProcessPaymentRequestDto;
import io.github.resilience4j.bulkhead.annotation.Bulkhead;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentClient {

    private final RestTemplate restTemplate;
    private static final String PAYMENT_SERVICE_URL = "http://PAYMENT-SERVICE/payments/process";

    @CircuitBreaker(name = "paymentService", fallbackMethod = "processPaymentFallback")
    @Retry(name = "paymentService")
    @Bulkhead(name = "paymentService")
    @RateLimiter(name = "paymentService")
    public PaymentResponseDto processPayment(ProcessPaymentRequestDto request) {
        log.info("Executing synchronous call to Payment Service for Order: {}", request.getOrderNumber());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<ProcessPaymentRequestDto> entity = new HttpEntity<>(request, headers);

        ResponseEntity<PaymentResponseDto> response = restTemplate.postForEntity(PAYMENT_SERVICE_URL, entity, PaymentResponseDto.class);

        if (response.getBody() != null) {
            PaymentResponseDto body = response.getBody();
            if ("FAILED".equalsIgnoreCase(body.getStatus()) || "UNAVAILABLE".equalsIgnoreCase(body.getStatus())) {
                log.warn("Payment Service returned failure status: {}", body.getStatus());
                throw new RuntimeException("Payment processing failed: " + body.getFailureReason());
            }
            return body;
        }
        throw new RuntimeException("Empty response from Payment Service");
    }

    public PaymentResponseDto processPaymentFallback(ProcessPaymentRequestDto request, Throwable throwable) {
        log.error("FALLBACK EXECUTED for Order: {} due to: {}", request.getOrderNumber(), throwable.toString());
        return PaymentResponseDto.builder()
                .orderNumber(request.getOrderNumber())
                .amount(request.getAmount())
                .status("FAILED")
                .failureReason("Circuit breaker / Fallback activated: " + throwable.getMessage())
                .fallbackExecuted(true)
                .build();
    }
}
